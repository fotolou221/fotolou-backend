package com.fotolou.app.config;

import com.fotolou.app.domain.*;
import com.fotolou.app.repository.*;
import com.fotolou.app.security.AuthoritiesConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final ProductCategoryRepository productCategoryRepository;

    public DatabaseDataInitializer(
        PlatformSettingsRepository platformSettingsRepository,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        ProductCategoryRepository productCategoryRepository
    ) {
        this.platformSettingsRepository = platformSettingsRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initAuthorities();
        initAdminUser();
        initPlatformSettings();
        initDefaultCategories();
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
        Optional<User> optAdmin = userRepository
            .findOneByLogin("admin@fotolou.sn")
            .or(() -> userRepository.findOneByLogin("admin"))
            .or(() -> userRepository.findOneByPhone("+221778627052"));

        User admin = optAdmin.orElseGet(User::new);
        admin.setLogin("admin@fotolou.sn");
        admin.setEmail("fotolou3@gmail.com");
        admin.setPhone("+221778627052");
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
        LOG.info("👤 Compte Super-Administrateur unique 'admin@fotolou.sn' (tél: +221778627052, email: fotolou3@gmail.com) synchronisé.");
    }

    private void initPlatformSettings() {
        PlatformSettings settings = platformSettingsRepository.findAll().stream().findFirst().orElseGet(PlatformSettings::new);
        settings.setAppName("Fotolou");
        settings.setContactEmail("fotolou3@gmail.com");
        settings.setContactPhone("+221 77 862 70 52");
        if (settings.getId() == null) {
            settings.setCommissionRate(10.0);
            settings.setOpeningTime("09:00");
            settings.setClosingTime("21:00");
            settings.setAllowRelativeBooking(true);
            settings.setMaintenanceMode(false);
        }
        platformSettingsRepository.save(settings);
        LOG.info("⚙️ Paramètres de la plateforme Fotolou synchronisés (tél: +221 77 862 70 52, email: fotolou3@gmail.com).");
    }

    private void initDefaultCategories() {
        if (productCategoryRepository.count() == 0) {
            ProductCategory cat1 = new ProductCategory();
            cat1.setName("Tondeuses & Matériel");
            cat1.setSlug("tondeuses");
            cat1.setDescription("Tondeuses de coupe, de finition et rasoirs professionnels");
            cat1.setImage("https://images.unsplash.com/photo-1503951914875-452162b0f3f1?auto=format&fit=crop&w=400&q=80");
            cat1.setIcon("scissors");
            productCategoryRepository.save(cat1);

            ProductCategory cat2 = new ProductCategory();
            cat2.setName("Soins & Huiles Barbe");
            cat2.setSlug("soins-barbe");
            cat2.setDescription("Huiles nourrissantes, baumes et shampoings pour barbe");
            cat2.setImage("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=400&q=80");
            cat2.setIcon("droplet");
            productCategoryRepository.save(cat2);

            ProductCategory cat3 = new ProductCategory();
            cat3.setName("Cires & Coiffants");
            cat3.setSlug("cires-coiffants");
            cat3.setDescription("Pommades, gels fixation forte et poudres texturisantes");
            cat3.setImage("https://images.unsplash.com/photo-1597354984706-fac992d9306f?auto=format&fit=crop&w=400&q=80");
            cat3.setIcon("sparkles");
            productCategoryRepository.save(cat3);

            LOG.info("🛍️ Catégories de boutique initiales créées avec succès.");
        }
    }
}
