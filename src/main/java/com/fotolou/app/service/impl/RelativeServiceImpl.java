package com.fotolou.app.service.impl;

import com.fotolou.app.domain.Relative;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.RelativeService;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.custom.otp.OtpService;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.mapper.RelativeMapper;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Relative}.
 */
@Service
@Transactional
public class RelativeServiceImpl implements RelativeService {

    private static final Logger LOG = LoggerFactory.getLogger(RelativeServiceImpl.class);

    private final RelativeRepository relativeRepository;
    private final RelativeMapper relativeMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;

    public RelativeServiceImpl(
        RelativeRepository relativeRepository,
        RelativeMapper relativeMapper,
        UserService userService,
        UserRepository userRepository,
        OtpService otpService
    ) {
        this.relativeRepository = relativeRepository;
        this.relativeMapper = relativeMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    @Override
    public RelativeDTO save(RelativeDTO relativeDTO) {
        LOG.debug("Request to save Relative : {}", relativeDTO);
        if (relativeDTO.getUser() == null) {
            com.fotolou.app.security.SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(u -> {
                    com.fotolou.app.service.dto.UserDTO userDTO = new com.fotolou.app.service.dto.UserDTO();
                    userDTO.setId(u.getId());
                    userDTO.setLogin(u.getLogin());
                    relativeDTO.setUser(userDTO);
                });
        }
        Relative relative = relativeMapper.toEntity(relativeDTO);
        if (relative.getCreatedDate() == null) {
            relative.setCreatedDate(Instant.now());
        }
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    @Override
    public RelativeDTO saveForUser(RelativeDTO relativeDTO, String login) {
        LOG.debug("Request to save Relative for user {} : {}", login, relativeDTO);
        User user = userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative.setUser(user);
        sanitizeRelativePhoneForUser(relative, user.getLogin(), null);
        if (relative.getCreatedDate() == null) {
            relative.setCreatedDate(Instant.now());
        }
        relative = saveRelativeWithPhoneGuard(relative);
        return relativeMapper.toDto(relative);
    }

    @Override
    public RelativeDTO update(RelativeDTO relativeDTO) {
        LOG.debug("Request to update Relative : {}", relativeDTO);
        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    @Override
    public RelativeDTO updateForUser(RelativeDTO relativeDTO, String login) {
        LOG.debug("Request to update Relative for user {} : {}", login, relativeDTO);
        Relative existing = relativeRepository
            .findByIdAndUserLogin(relativeDTO.getId(), login)
            .orElseThrow(() -> new IllegalArgumentException("Ce proche est introuvable ou ne vous appartient pas"));

        existing.setName(relativeDTO.getName());
        existing.setRelation(relativeDTO.getRelation());
        existing.setPhone(relativeDTO.getPhone());
        sanitizeRelativePhoneForUser(existing, login, existing.getId());
        existing = saveRelativeWithPhoneGuard(existing);
        return relativeMapper.toDto(existing);
    }

    @Override
    public Optional<RelativeDTO> partialUpdate(RelativeDTO relativeDTO) {
        LOG.debug("Request to partially update Relative : {}", relativeDTO);

        return relativeRepository
            .findById(relativeDTO.getId())
            .map(existingRelative -> {
                relativeMapper.partialUpdate(existingRelative, relativeDTO);
                return existingRelative;
            })
            .map(relativeRepository::save)
            .map(relativeMapper::toDto);
    }

    @Override
    public Optional<RelativeDTO> partialUpdateForUser(RelativeDTO relativeDTO, String login) {
        LOG.debug("Request to partially update Relative for user {} : {}", login, relativeDTO);
        return relativeRepository
            .findByIdAndUserLogin(relativeDTO.getId(), login)
            .map(existing -> {
                if (relativeDTO.getName() != null) {
                    existing.setName(relativeDTO.getName());
                }
                if (relativeDTO.getRelation() != null) {
                    existing.setRelation(relativeDTO.getRelation());
                }
                if (relativeDTO.getPhone() != null) {
                    existing.setPhone(relativeDTO.getPhone());
                }
                sanitizeRelativePhoneForUser(existing, login, existing.getId());
                return saveRelativeWithPhoneGuard(existing);
            })
            .map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelativeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Relatives");
        return relativeRepository.findAll(pageable).map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelativeDTO> findAllForUser(String login, Pageable pageable) {
        LOG.debug("Request to get all Relatives for user : {}", login);
        return relativeRepository.findByUserLogin(login, pageable).map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelativeDTO> findOne(Long id) {
        LOG.debug("Request to get Relative : {}", id);
        return relativeRepository.findById(id).map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelativeDTO> findOneForUser(Long id, String login) {
        LOG.debug("Request to get Relative {} for user : {}", id, login);
        return relativeRepository.findByIdAndUserLogin(id, login).map(relativeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Relative : {}", id);
        relativeRepository.deleteById(id);
    }

    @Override
    public void deleteForUser(Long id, String login) {
        LOG.debug("Request to delete Relative {} for user : {}", id, login);
        Relative relative = relativeRepository
            .findByIdAndUserLogin(id, login)
            .orElseThrow(() -> new IllegalArgumentException("Ce proche est introuvable ou ne vous appartient pas"));
        relativeRepository.delete(relative);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return relativeRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndUser(Long id, String login) {
        return relativeRepository.existsByIdAndUserLogin(id, login);
    }

    private void sanitizeRelativePhoneForUser(Relative relative, String login, Long excludedRelativeId) {
        String normalizedPhone = normalizeOptionalPhone(relative.getPhone());
        relative.setPhone(normalizedPhone);

        if (normalizedPhone == null) {
            return;
        }

        String accountPhone = normalizePhoneForCompare(login);
        if (accountPhone != null && normalizedPhone.equals(accountPhone)) {
            throw new IllegalArgumentException("Vous ne pouvez pas ajouter votre propre numero comme proche.");
        }

        boolean alreadyUsedByAnotherRelative = relativeRepository
            .findByUserLogin(login)
            .stream()
            .anyMatch(
                existing ->
                    !Objects.equals(existing.getId(), excludedRelativeId) &&
                    normalizedPhone.equals(normalizePhoneForCompare(existing.getPhone()))
            );

        if (alreadyUsedByAnotherRelative) {
            throw new IllegalArgumentException("Ce numero de telephone est deja utilise par un autre proche.");
        }
    }

    private String normalizeOptionalPhone(String rawPhone) {
        String normalized = otpService.normalizePhoneNumber(rawPhone);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        int digitsCount = normalized.replaceAll("[^0-9]", "").length();
        if (digitsCount < 9) {
            throw new IllegalArgumentException("Numero de telephone invalide.");
        }

        return normalized;
    }

    private String normalizePhoneForCompare(String rawPhone) {
        String normalized = otpService.normalizePhoneNumber(rawPhone);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private Relative saveRelativeWithPhoneGuard(Relative relative) {
        try {
            return relativeRepository.saveAndFlush(relative);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ce numero de telephone est deja utilise par un autre proche.");
        }
    }
}
