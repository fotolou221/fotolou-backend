package com.fotolou.app.service.impl;

import com.fotolou.app.config.Constants;
import com.fotolou.app.domain.Authority;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.AuthorityRepository;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.AuthoritiesConstants;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.EmailAlreadyUsedException;
import com.fotolou.app.service.InvalidPasswordException;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.UsernameAlreadyUsedException;
import com.fotolou.app.service.dto.AdminUserDTO;
import com.fotolou.app.service.dto.UserDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final CacheManager cacheManager;
    private final TicketRepository ticketRepository;
    private final RelativeRepository relativeRepository;

    public UserServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager,
        TicketRepository ticketRepository,
        RelativeRepository relativeRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.ticketRepository = ticketRepository;
        this.relativeRepository = relativeRepository;
    }

    @Override
    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key).map(user -> {
            user.setActivated(true);
            user.setActivationKey(null);
            this.clearUserCaches(user);
            LOG.debug("Activated user: {}", user);
            return user;
        });
    }

    @Override
    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
            });
    }

    @Override
    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    @Override
    public User registerUser(AdminUserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        newUser.setActivated(false);
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    @Override
    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        LOG.debug("Created Information for User: {}", user);
        return user;
    }

    @Override
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    @Override
    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            this.clearUserCaches(user);
            LOG.debug("Deleted User: {}", user);
        });
    }

    @Override
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
            });
    }

    @Override
    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                LOG.debug("Changed password for User: {}", user);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        List<User> users = usersPage.getContent();
        if (users.isEmpty()) {
            return usersPage.map(AdminUserDTO::new);
        }

        List<Long> userIds = users.stream().map(User::getId).filter(Objects::nonNull).toList();
        List<String> userPhones = users
            .stream()
            .flatMap(u -> java.util.stream.Stream.of(u.getLogin(), u.getPhone()))
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .distinct()
            .toList();

        Map<Long, Long> relativesMap = new HashMap<>();
        if (!userIds.isEmpty() && relativeRepository != null) {
            for (Object[] row : relativeRepository.countRelativesByUserIds(userIds)) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    relativesMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
                }
            }
        }

        Map<Long, Long> ticketsUserMap = new HashMap<>();
        if (!userIds.isEmpty() && ticketRepository != null) {
            for (Object[] row : ticketRepository.countTicketsByUserIds(userIds)) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    ticketsUserMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
                }
            }
        }

        Map<String, Long> ticketsPhoneMap = new HashMap<>();
        if (!userPhones.isEmpty() && ticketRepository != null) {
            for (Object[] row : ticketRepository.countTicketsByOwnerPhonesWithoutUser(userPhones)) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    ticketsPhoneMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
                }
            }
        }

        return usersPage.map(u -> {
            AdminUserDTO dto = new AdminUserDTO(u);
            Long rCount = relativesMap.getOrDefault(u.getId(), 0L);
            Long tCount = ticketsUserMap.getOrDefault(u.getId(), 0L);
            if (u.getPhone() != null && ticketsPhoneMap.containsKey(u.getPhone())) {
                tCount += ticketsPhoneMap.get(u.getPhone());
            }
            if (u.getLogin() != null && !Objects.equals(u.getLogin(), u.getPhone()) && ticketsPhoneMap.containsKey(u.getLogin())) {
                tCount += ticketsPhoneMap.get(u.getLogin());
            }
            dto.setRelativesCount(rCount);
            dto.setTicketsCount(tCount);
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findOneByEmailIgnoreCase(String email) {
        return userRepository.findOneByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findOneByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminUserDTO> getManagedUser(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login).map(u -> {
            AdminUserDTO dto = new AdminUserDTO(u);
            long rCount = relativeRepository != null ? relativeRepository.countByUserId(u.getId()) : 0L;
            long tCount =
                ticketRepository != null
                    ? ticketRepository.countByUserIdOrPhone(u.getId(), u.getPhone() != null ? u.getPhone() : u.getLogin())
                    : 0L;
            dto.setRelativesCount(rCount);
            dto.setTicketsCount(tCount);
            return dto;
        });
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evictIfPresent(user.getEmail());
        }
    }
}
