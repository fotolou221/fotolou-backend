package com.fotolou.app.service;

import com.fotolou.app.service.dto.FavoriteSalonDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.FavoriteSalon}.
 */
public interface FavoriteSalonService {
    /**
     * Save a favoriteSalon.
     *
     * @param favoriteSalonDTO the entity to save.
     * @return the persisted entity.
     */
    FavoriteSalonDTO save(FavoriteSalonDTO favoriteSalonDTO);

    /**
     * Update a favoriteSalon.
     *
     * @param favoriteSalonDTO the entity to save.
     * @return the persisted entity.
     */
    FavoriteSalonDTO update(FavoriteSalonDTO favoriteSalonDTO);

    /**
     * Partially update a favoriteSalon.
     *
     * @param favoriteSalonDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FavoriteSalonDTO> partialUpdate(FavoriteSalonDTO favoriteSalonDTO);

    /**
     * Get all the favoriteSalons.
     *
     * @return the list of entities.
     */
    List<FavoriteSalonDTO> findAll();

    /**
     * Get one favoriteSalon by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FavoriteSalonDTO> findOne(Long id);

    /**
     * Delete the favoriteSalon by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a favoriteSalon exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
