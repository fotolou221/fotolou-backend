package com.fotolou.app.service;

import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.repository.AppNotificationRepository;
import com.fotolou.app.service.dto.AppNotificationDTO;
import com.fotolou.app.service.mapper.AppNotificationMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.AppNotification}.
 */
@Service
@Transactional
public class AppNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(AppNotificationService.class);

    private final AppNotificationRepository appNotificationRepository;

    private final AppNotificationMapper appNotificationMapper;

    public AppNotificationService(AppNotificationRepository appNotificationRepository, AppNotificationMapper appNotificationMapper) {
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
    }

    /**
     * Save a appNotification.
     *
     * @param appNotificationDTO the entity to save.
     * @return the persisted entity.
     */
    public AppNotificationDTO save(AppNotificationDTO appNotificationDTO) {
        LOG.debug("Request to save AppNotification : {}", appNotificationDTO);
        AppNotification appNotification = appNotificationMapper.toEntity(appNotificationDTO);
        appNotification = appNotificationRepository.save(appNotification);
        return appNotificationMapper.toDto(appNotification);
    }

    /**
     * Update a appNotification.
     *
     * @param appNotificationDTO the entity to save.
     * @return the persisted entity.
     */
    public AppNotificationDTO update(AppNotificationDTO appNotificationDTO) {
        LOG.debug("Request to update AppNotification : {}", appNotificationDTO);
        AppNotification appNotification = appNotificationMapper.toEntity(appNotificationDTO);
        appNotification = appNotificationRepository.save(appNotification);
        return appNotificationMapper.toDto(appNotification);
    }

    /**
     * Partially update a appNotification.
     *
     * @param appNotificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AppNotificationDTO> partialUpdate(AppNotificationDTO appNotificationDTO) {
        LOG.debug("Request to partially update AppNotification : {}", appNotificationDTO);

        return appNotificationRepository
            .findById(appNotificationDTO.getId())
            .map(existingAppNotification -> {
                appNotificationMapper.partialUpdate(existingAppNotification, appNotificationDTO);

                return existingAppNotification;
            })
            .map(appNotificationRepository::save)
            .map(appNotificationMapper::toDto);
    }

    /**
     * Get one appNotification by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AppNotificationDTO> findOne(Long id) {
        LOG.debug("Request to get AppNotification : {}", id);
        return appNotificationRepository.findById(id).map(appNotificationMapper::toDto);
    }

    /**
     * Delete the appNotification by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AppNotification : {}", id);
        appNotificationRepository.deleteById(id);
    }
}
