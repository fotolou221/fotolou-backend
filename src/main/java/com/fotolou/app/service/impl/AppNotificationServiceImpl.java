package com.fotolou.app.service.impl;

import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.repository.AppNotificationRepository;
import com.fotolou.app.service.AppNotificationService;
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
public class AppNotificationServiceImpl implements AppNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(AppNotificationServiceImpl.class);

    private final AppNotificationRepository appNotificationRepository;
    private final AppNotificationMapper appNotificationMapper;

    public AppNotificationServiceImpl(AppNotificationRepository appNotificationRepository, AppNotificationMapper appNotificationMapper) {
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
    }

    @Override
    public AppNotificationDTO save(AppNotificationDTO appNotificationDTO) {
        LOG.debug("Request to save AppNotification : {}", appNotificationDTO);
        AppNotification appNotification = appNotificationMapper.toEntity(appNotificationDTO);
        appNotification = appNotificationRepository.save(appNotification);
        return appNotificationMapper.toDto(appNotification);
    }

    @Override
    public AppNotificationDTO update(AppNotificationDTO appNotificationDTO) {
        LOG.debug("Request to update AppNotification : {}", appNotificationDTO);
        AppNotification appNotification = appNotificationMapper.toEntity(appNotificationDTO);
        appNotification = appNotificationRepository.save(appNotification);
        return appNotificationMapper.toDto(appNotification);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Optional<AppNotificationDTO> findOne(Long id) {
        LOG.debug("Request to get AppNotification : {}", id);
        return appNotificationRepository.findById(id).map(appNotificationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AppNotification : {}", id);
        appNotificationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return appNotificationRepository.existsById(id);
    }
}
