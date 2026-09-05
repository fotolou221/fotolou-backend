package com.fotolou.app.service;

import com.fotolou.app.service.dto.CoiffeurProfileDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.CoiffeurProfile}.
 */
public interface CoiffeurProfileService {
    /**
     * Save a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to save.
     * @return the persisted entity.
     */
    CoiffeurProfileDTO save(CoiffeurProfileDTO coiffeurProfileDTO);

    /**
     * Update a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to save.
     * @return the persisted entity.
     */
    CoiffeurProfileDTO update(CoiffeurProfileDTO coiffeurProfileDTO);

    /**
     * Partially update a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CoiffeurProfileDTO> partialUpdate(CoiffeurProfileDTO coiffeurProfileDTO);

    /**
     * Get all the coiffeurProfiles with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<CoiffeurProfileDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one coiffeurProfile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CoiffeurProfileDTO> findOne(Long id);

    /**
     * Delete the coiffeurProfile by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a coiffeurProfile exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
