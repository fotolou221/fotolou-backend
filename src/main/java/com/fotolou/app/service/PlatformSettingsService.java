package com.fotolou.app.service;

import com.fotolou.app.service.dto.PlatformSettingsDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.PlatformSettings}.
 */
public interface PlatformSettingsService {
    /**
     * Save a platformSettings.
     *
     * @param platformSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    PlatformSettingsDTO save(PlatformSettingsDTO platformSettingsDTO);

    /**
     * Update a platformSettings.
     *
     * @param platformSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    PlatformSettingsDTO update(PlatformSettingsDTO platformSettingsDTO);

    /**
     * Partially update a platformSettings.
     *
     * @param platformSettingsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PlatformSettingsDTO> partialUpdate(PlatformSettingsDTO platformSettingsDTO);

    /**
     * Get all the platformSettingses.
     *
     * @return the list of entities.
     */
    List<PlatformSettingsDTO> findAll();

    /**
     * Get one platformSettings by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PlatformSettingsDTO> findOne(Long id);

    /**
     * Delete the platformSettings by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a platformSettings exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
