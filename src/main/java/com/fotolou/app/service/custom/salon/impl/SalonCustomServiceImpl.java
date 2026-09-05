package com.fotolou.app.service.custom.salon.impl;

import com.fotolou.app.domain.FavoriteSalon;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.repository.FavoriteSalonRepository;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.custom.salon.SalonCustomService;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.SalonMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service des opérations personnalisées sur les Salons et les Favoris.
 */
@Service
@Transactional
public class SalonCustomServiceImpl implements SalonCustomService {

    private static final Logger LOG = LoggerFactory.getLogger(SalonCustomServiceImpl.class);

    private final SalonRepository salonRepository;
    private final FavoriteSalonRepository favoriteSalonRepository;
    private final UserRepository userRepository;
    private final SalonMapper salonMapper;
    private final com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService;

    public SalonCustomServiceImpl(
        SalonRepository salonRepository,
        FavoriteSalonRepository favoriteSalonRepository,
        UserRepository userRepository,
        SalonMapper salonMapper,
        com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService
    ) {
        this.salonRepository = salonRepository;
        this.favoriteSalonRepository = favoriteSalonRepository;
        this.userRepository = userRepository;
        this.salonMapper = salonMapper;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public ToggleStatusResult toggleSalonStatus(Long salonId) {
        Salon salon = salonRepository
            .findById(salonId)
            .orElseThrow(() -> new IllegalArgumentException("Salon introuvable ID : " + salonId));

        SalonStatus newStatus = salon.getStatus() == SalonStatus.OPEN ? SalonStatus.CLOSED : SalonStatus.OPEN;
        salon.setStatus(newStatus);
        salon.setLastModifiedDate(Instant.now());
        salonRepository.save(salon);

        LOG.info("🔄 Statut du salon '{}' changé en {}", salon.getName(), newStatus);
        realtimeEventService.broadcast("SALON_UPDATED", salonMapper.toDto(salon));
        return new ToggleStatusResult(salon.getId(), newStatus.name(), "Statut du salon mis à jour : " + newStatus.name());
    }

    @Override
    public ToggleFavoriteResult toggleFavorite(Long salonId, String userLogin) {
        if (userLogin == null) {
            throw new IllegalArgumentException("Authentification requise.");
        }

        User user = userRepository.findOneByLogin(userLogin).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        Salon salon = salonRepository.findById(salonId).orElseThrow(() -> new IllegalArgumentException("Salon introuvable."));

        Optional<FavoriteSalon> optFav = favoriteSalonRepository.findByUserIdAndSalonId(user.getId(), salonId);
        if (optFav.isPresent()) {
            favoriteSalonRepository.delete(optFav.get());
            LOG.info("⭐ Salon {} retiré des favoris de {}", salonId, user.getLogin());
            return new ToggleFavoriteResult(salonId, false, "Salon retiré des favoris");
        } else {
            FavoriteSalon fav = new FavoriteSalon();
            fav.setUser(user);
            fav.setSalon(salon);
            fav.setCreatedDate(Instant.now());
            favoriteSalonRepository.save(fav);
            LOG.info("⭐ Salon {} ajouté aux favoris de {}", salonId, user.getLogin());
            return new ToggleFavoriteResult(salonId, true, "Salon ajouté aux favoris");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalonDTO> getMyFavorites(String userLogin) {
        if (userLogin == null) {
            return Collections.emptyList();
        }

        Optional<User> optUser = userRepository.findOneByLogin(userLogin);
        if (optUser.isEmpty()) {
            return Collections.emptyList();
        }

        List<FavoriteSalon> favorites = favoriteSalonRepository.findWithSalonByUserId(optUser.get().getId());
        return favorites.stream().map(FavoriteSalon::getSalon).filter(Objects::nonNull).map(salonMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalonDTO> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return salonRepository.findOneBySlug(slug).map(salonMapper::toDto);
    }
}
