package com.fotolou.app.service.custom.salon;

import com.fotolou.app.service.dto.SalonDTO;
import java.util.List;

/**
 * Interface de contrat pour les opérations métier sur les Salons et les Favoris.
 */
public interface SalonCustomService {
    record ToggleStatusResult(Long id, String status, String message) {}

    record ToggleFavoriteResult(Long salonId, boolean isFavorite, String message) {}

    /**
     * Ouvre ou ferme la file d'un salon.
     */
    ToggleStatusResult toggleSalonStatus(Long salonId);

    /**
     * Ajoute ou retire un salon des favoris du client connecté.
     */
    ToggleFavoriteResult toggleFavorite(Long salonId, String userLogin);

    /**
     * Récupère la liste des salons favoris du client connecté.
     */
    List<SalonDTO> getMyFavorites(String userLogin);

    /**
     * Récupère un salon par son slug.
     */
    java.util.Optional<SalonDTO> findBySlug(String slug);
}
