package com.fotolou.app.config;

import com.fotolou.app.domain.*;
import com.fotolou.app.repository.*;
import com.fotolou.app.security.AuthoritiesConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialisateur de démarrage Fotolou : garantit l'existence des rôles,
 * du super-administrateur unique et des paramètres de la plateforme.
 */
@Component
public class DatabaseDataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseDataInitializer.class);

    private final PlatformSettingsRepository platformSettingsRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseDataInitializer(
        PlatformSettingsRepository platformSettingsRepository,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.platformSettingsRepository = platformSettingsRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initAuthorities();
        initAdminUser();
        initPlatformSettings();
        LOG.info("🚀 Fotolou initialisé en mode propre (Prêt pour la production).");
    }

    private void initAuthorities() {
        List<String> roles = List.of(
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.USER,
            AuthoritiesConstants.CLIENT,
            AuthoritiesConstants.COIFFEUR,
            AuthoritiesConstants.SUPER_ADMIN
        );
        for (String r : roles) {
            if (!authorityRepository.existsById(r)) {
                Authority auth = new Authority();
                auth.setName(r);
                authorityRepository.save(auth);
            }
        }
    }

    private void initAdminUser() {
        if (userRepository.findOneByLogin("admin@fotolou.sn").isEmpty()) {
            User admin = new User();
            admin.setLogin("admin@fotolou.sn");
            admin.setEmail("admin@fotolou.sn");
            admin.setPassword(passwordEncoder.encode("admin_fotolou_2026"));
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setActivated(true);
            admin.setLangKey("fr");

            Set<Authority> authorities = new HashSet<>();
            authorityRepository.findById(AuthoritiesConstants.ADMIN).ifPresent(authorities::add);
            authorityRepository.findById(AuthoritiesConstants.SUPER_ADMIN).ifPresent(authorities::add);
            authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
            admin.setAuthorities(authorities);
            userRepository.save(admin);
            LOG.info("👤 Compte Super-Administrateur unique 'admin@fotolou.sn' vérifié/créé.");
        }
    }

    private void initPlatformSettings() {
        if (platformSettingsRepository.count() == 0) {
            PlatformSettings settings = new PlatformSettings();
            settings.setAppName("Fotolou");
            settings.setContactEmail("support@fotolou.sn");
            settings.setContactPhone("+221 77 862 70 52");
            settings.setCommissionRate(10.0);
            settings.setOpeningTime("09:00");
            settings.setClosingTime("21:00");
            settings.setAllowRelativeBooking(true);
            settings.setMaintenanceMode(false);
            platformSettingsRepository.save(settings);
            LOG.info("⚙️ Paramètres initiaux de la plateforme Fotolou configurés.");
        }
    }
}
