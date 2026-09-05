package com.fotolou.app.service;

import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.BoutiqueOrder}.
 */
public interface BoutiqueOrderService {
    /**
     * Save a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to save.
     * @return the persisted entity.
     */
    BoutiqueOrderDTO save(BoutiqueOrderDTO boutiqueOrderDTO);

    /**
     * Update a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to save.
     * @return the persisted entity.
     */
    BoutiqueOrderDTO update(BoutiqueOrderDTO boutiqueOrderDTO);

    /**
     * Partially update a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BoutiqueOrderDTO> partialUpdate(BoutiqueOrderDTO boutiqueOrderDTO);

    /**
     * Get one boutiqueOrder by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BoutiqueOrderDTO> findOne(Long id);

    /**
     * Delete the boutiqueOrder by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a boutiqueOrder exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
