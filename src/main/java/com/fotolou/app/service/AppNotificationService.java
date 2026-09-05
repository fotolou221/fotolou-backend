package com.fotolou.app.service;

import com.fotolou.app.service.dto.AppNotificationDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.AppNotification}.
 */
public interface AppNotificationService {
    /**
     * Save a appNotification.
     *
     * @param appNotificationDTO the entity to save.
     * @return the persisted entity.
     */
    AppNotificationDTO save(AppNotificationDTO appNotificationDTO);

    /**
     * Update a appNotification.
     *
     * @param appNotificationDTO the entity to save.
     * @return the persisted entity.
     */
    AppNotificationDTO update(AppNotificationDTO appNotificationDTO);

    /**
     * Partially update a appNotification.
     *
     * @param appNotificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AppNotificationDTO> partialUpdate(AppNotificationDTO appNotificationDTO);

    /**
     * Get one appNotification by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppNotificationDTO> findOne(Long id);

    /**
     * Delete the appNotification by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if an appNotification exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
