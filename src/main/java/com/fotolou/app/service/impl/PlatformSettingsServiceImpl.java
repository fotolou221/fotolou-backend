package com.fotolou.app.service.impl;

import com.fotolou.app.domain.PlatformSettings;
import com.fotolou.app.repository.PlatformSettingsRepository;
import com.fotolou.app.service.PlatformSettingsService;
import com.fotolou.app.service.dto.PlatformSettingsDTO;
import com.fotolou.app.service.mapper.PlatformSettingsMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.PlatformSettings}.
 */
@Service
@Transactional
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformSettingsServiceImpl.class);

    private final PlatformSettingsRepository platformSettingsRepository;
    private final PlatformSettingsMapper platformSettingsMapper;

    public PlatformSettingsServiceImpl(
        PlatformSettingsRepository platformSettingsRepository,
        PlatformSettingsMapper platformSettingsMapper
    ) {
        this.platformSettingsRepository = platformSettingsRepository;
        this.platformSettingsMapper = platformSettingsMapper;
    }

    @Override
    public PlatformSettingsDTO save(PlatformSettingsDTO platformSettingsDTO) {
        LOG.debug("Request to save PlatformSettings : {}", platformSettingsDTO);
        PlatformSettings platformSettings = platformSettingsMapper.toEntity(platformSettingsDTO);
        platformSettings = platformSettingsRepository.save(platformSettings);
        return platformSettingsMapper.toDto(platformSettings);
    }

    @Override
    public PlatformSettingsDTO update(PlatformSettingsDTO platformSettingsDTO) {
        LOG.debug("Request to update PlatformSettings : {}", platformSettingsDTO);
        PlatformSettings platformSettings = platformSettingsMapper.toEntity(platformSettingsDTO);
        platformSettings = platformSettingsRepository.save(platformSettings);
        return platformSettingsMapper.toDto(platformSettings);
    }

    @Override
    public Optional<PlatformSettingsDTO> partialUpdate(PlatformSettingsDTO platformSettingsDTO) {
        LOG.debug("Request to partially update PlatformSettings : {}", platformSettingsDTO);

        return platformSettingsRepository
            .findById(platformSettingsDTO.getId())
            .map(existingPlatformSettings -> {
                platformSettingsMapper.partialUpdate(existingPlatformSettings, platformSettingsDTO);
                return existingPlatformSettings;
            })
            .map(platformSettingsRepository::save)
            .map(platformSettingsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformSettingsDTO> findAll() {
        LOG.debug("Request to get all PlatformSettingses");
        return platformSettingsRepository
            .findAll()
            .stream()
            .map(platformSettingsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlatformSettingsDTO> findOne(Long id) {
        LOG.debug("Request to get PlatformSettings : {}", id);
        return platformSettingsRepository.findById(id).map(platformSettingsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete PlatformSettings : {}", id);
        platformSettingsRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return platformSettingsRepository.existsById(id);
    }
}
