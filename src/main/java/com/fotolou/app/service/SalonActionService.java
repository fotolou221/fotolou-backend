package com.fotolou.app.service;

import com.fotolou.app.service.dto.SalonActionDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.SalonAction}.
 */
public interface SalonActionService {
    /**
     * Save a salonAction.
     *
     * @param salonActionDTO the entity to save.
     * @return the persisted entity.
     */
    SalonActionDTO save(SalonActionDTO salonActionDTO);

    /**
     * Update a salonAction.
     *
     * @param salonActionDTO the entity to save.
     * @return the persisted entity.
     */
    SalonActionDTO update(SalonActionDTO salonActionDTO);

    /**
     * Partially update a salonAction.
     *
     * @param salonActionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SalonActionDTO> partialUpdate(SalonActionDTO salonActionDTO);

    /**
     * Get all the salonActions.
     *
     * @return the list of entities.
     */
    List<SalonActionDTO> findAll();

    /**
     * Get all the salonActions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<SalonActionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one salonAction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SalonActionDTO> findOne(Long id);

    /**
     * Delete the salonAction by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a salonAction exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
