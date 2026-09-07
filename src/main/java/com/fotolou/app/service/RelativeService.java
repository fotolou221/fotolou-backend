package com.fotolou.app.service;

import com.fotolou.app.service.dto.RelativeDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.Relative}.
 */
public interface RelativeService {
    /**
     * Save a relative.
     *
     * @param relativeDTO the entity to save.
     * @return the persisted entity.
     */
    RelativeDTO save(RelativeDTO relativeDTO);

    /**
     * Update a relative.
     *
     * @param relativeDTO the entity to save.
     * @return the persisted entity.
     */
    RelativeDTO update(RelativeDTO relativeDTO);

    /**
     * Partially update a relative.
     *
     * @param relativeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<RelativeDTO> partialUpdate(RelativeDTO relativeDTO);

    /**
     * Get all the relatives.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<RelativeDTO> findAll(Pageable pageable);

    /**
     * Get one relative by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RelativeDTO> findOne(Long id);

    /**
     * Delete the relative by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a relative exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);

    /**
     * Save a relative for a specific user.
     */
    RelativeDTO saveForUser(RelativeDTO relativeDTO, String login);

    /**
     * Update a relative owned by a specific user.
     */
    RelativeDTO updateForUser(RelativeDTO relativeDTO, String login);

    /**
     * Partially update a relative owned by a specific user.
     */
    Optional<RelativeDTO> partialUpdateForUser(RelativeDTO relativeDTO, String login);

    /**
     * Get all the relatives for a specific user.
     */
    Page<RelativeDTO> findAllForUser(String login, Pageable pageable);

    /**
     * Get one relative by id for a specific user.
     */
    Optional<RelativeDTO> findOneForUser(Long id, String login);

    /**
     * Delete the relative by id for a specific user.
     */
    void deleteForUser(Long id, String login);

    /**
     * Check if a relative exists by id and belongs to a specific user.
     */
    boolean existsByIdAndUser(Long id, String login);
}
