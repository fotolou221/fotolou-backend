package com.fotolou.app.web.rest.custom;

import com.fotolou.app.domain.FavoriteSalon;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.repository.FavoriteSalonRepository;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.SalonMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST métier pour les opérations sur les Salons et les Favoris.
 */
@Tag(name = "2. Salons & Barbiers", description = "Recherche de salons et état d'ouverture")
@RestController
@RequestMapping("/api")
@Transactional
public class SalonCustomResource {

    private static final Logger LOG = LoggerFactory.getLogger(SalonCustomResource.class);

    private final SalonRepository salonRepository;
    private final FavoriteSalonRepository favoriteSalonRepository;
    private final UserRepository userRepository;
    private final SalonMapper salonMapper;

    public SalonCustomResource(
        SalonRepository salonRepository,
        FavoriteSalonRepository favoriteSalonRepository,
        UserRepository userRepository,
        SalonMapper salonMapper
    ) {
        this.salonRepository = salonRepository;
        this.favoriteSalonRepository = favoriteSalonRepository;
        this.userRepository = userRepository;
        this.salonMapper = salonMapper;
    }

    public record ToggleStatusResponse(Long id, String status, String message) {}

    public record ToggleFavoriteRequest(Long salonId) {}

    public record ToggleFavoriteResponse(Long salonId, boolean isFavorite, String message) {}

    /**
     * PATCH /api/salons/{id}/toggle-status : Ouvre ou ferme la file du salon (accessible aux coiffeurs & admins).
     */
    @PatchMapping("/salons/{id}/toggle-status")
    public ResponseEntity<?> toggleSalonStatus(@PathVariable Long id) {
        Optional<Salon> optSalon = salonRepository.findById(id);
        if (optSalon.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Salon salon = optSalon.get();
        SalonStatus newStatus = salon.getStatus() == SalonStatus.OPEN ? SalonStatus.CLOSED : SalonStatus.OPEN;
        salon.setStatus(newStatus);
        salon.setLastModifiedDate(Instant.now());
        salonRepository.save(salon);

        LOG.info("🔄 Statut du salon '{}' changé en {}", salon.getName(), newStatus);
        return ResponseEntity.ok(
            new ToggleStatusResponse(salon.getId(), newStatus.name(), "Statut du salon mis à jour : " + newStatus.name())
        );
    }

    /**
     * POST /api/favorites/toggle : Ajoute ou retire un salon des favoris du client connecté.
     */
    @Tag(name = "5. Salons Favoris", description = "Ajout, retrait et consultation des salons favoris")
    @PostMapping("/favorites/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestBody ToggleFavoriteRequest request) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentification requise."));
        }

        User user = userRepository.findOneByLogin(currentLogin).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Utilisateur introuvable."));
        }

        Long salonId = request.salonId();
        Optional<Salon> optSalon = salonRepository.findById(salonId);
        if (optSalon.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Salon introuvable."));
        }

        Optional<FavoriteSalon> optFav = favoriteSalonRepository.findByUserIdAndSalonId(user.getId(), salonId);
        if (optFav.isPresent()) {
            favoriteSalonRepository.delete(optFav.get());
            LOG.info("⭐ Salon {} retiré des favoris de {}", salonId, user.getLogin());
            return ResponseEntity.ok(new ToggleFavoriteResponse(salonId, false, "Salon retiré des favoris"));
        } else {
            FavoriteSalon fav = new FavoriteSalon();
            fav.setUser(user);
            fav.setSalon(optSalon.get());
            fav.setCreatedDate(Instant.now());
            favoriteSalonRepository.save(fav);
            LOG.info("⭐ Salon {} ajouté aux favoris de {}", salonId, user.getLogin());
            return ResponseEntity.ok(new ToggleFavoriteResponse(salonId, true, "Salon ajouté aux favoris"));
        }
    }

    /**
     * GET /api/favorites/my-favorites : Liste complète des salons favoris du client.
     */
    @Tag(name = "5. Salons Favoris", description = "Ajout, retrait et consultation des salons favoris")
    @GetMapping("/favorites/my-favorites")
    public ResponseEntity<List<SalonDTO>> getMyFavorites() {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        User user = userRepository.findOneByLogin(currentLogin).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<FavoriteSalon> favorites = favoriteSalonRepository.findWithSalonByUserId(user.getId());
        List<SalonDTO> salonList = favorites
            .stream()
            .map(FavoriteSalon::getSalon)
            .filter(Objects::nonNull)
            .map(salonMapper::toDto)
            .toList();

        return ResponseEntity.ok(salonList);
    }
}
