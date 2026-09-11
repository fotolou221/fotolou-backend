package com.fotolou.app.security;

import com.fotolou.app.domain.Authority;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.UserRepository;
import java.util.*;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String login) {
        LOG.debug("Authenticating {}", login);

        String lowercaseLogin = login.trim().toLowerCase(Locale.ENGLISH);
        String digitsOnly = lowercaseLogin.replaceAll("[^0-9]", "");

        return userRepository
            .findOneWithAuthoritiesByLogin(lowercaseLogin)
            .or(() -> userRepository.findOneWithAuthoritiesByEmailIgnoreCase(lowercaseLogin))
            .or(() -> userRepository.findOneWithAuthoritiesByPhone(lowercaseLogin))
            .or(() ->
                digitsOnly.length() >= 9
                    ? userRepository.findOneWithAuthoritiesByPhone("+221" + digitsOnly.substring(digitsOnly.length() - 9))
                    : Optional.empty()
            )
            .or(() ->
                digitsOnly.length() >= 9
                    ? userRepository.findOneWithAuthoritiesByPhone(digitsOnly.substring(digitsOnly.length() - 9))
                    : Optional.empty()
            )
            .or(() ->
                digitsOnly.length() >= 9
                    ? userRepository.findOneWithAuthoritiesByLogin("+221" + digitsOnly.substring(digitsOnly.length() - 9))
                    : Optional.empty()
            )
            .or(() ->
                digitsOnly.length() >= 9
                    ? userRepository.findOneWithAuthoritiesByLogin(digitsOnly.substring(digitsOnly.length() - 9))
                    : Optional.empty()
            )
            .map(user -> createSpringSecurityUser(user.getLogin(), user))
            .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, User user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        return UserWithId.fromUser(user);
    }

    public static class UserWithId extends org.springframework.security.core.userdetails.User {

        private final Long id;

        public UserWithId(String login, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(login, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public static UserWithId fromUser(User user) {
            return new UserWithId(
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities().stream().map(Authority::getName).map(SimpleGrantedAuthority::new).toList(),
                user.getId()
            );
        }
    }
}
