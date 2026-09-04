package com.fotolou.app.service;

import com.fotolou.app.domain.PlatformSettings;
import com.fotolou.app.repository.PlatformSettingsRepository;
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
public class PlatformSettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformSettingsService.class);

    private final PlatformSettingsRepository platformSettingsRepository;

    private final PlatformSettingsMapper platformSettingsMapper;

    public PlatformSettingsService(PlatformSettingsRepository platformSettingsRepository, PlatformSettingsMapper platformSettingsMapper) {
        this.platformSettingsRepository = platformSettingsRepository;
        this.platformSettingsMapper = platformSettingsMapper;
    }

    /**
     * Save a platformSettings.
     *
     * @param platformSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    public PlatformSettingsDTO save(PlatformSettingsDTO platformSettingsDTO) {
        LOG.debug("Request to save PlatformSettings : {}", platformSettingsDTO);
        PlatformSettings platformSettings = platformSettingsMapper.toEntity(platformSettingsDTO);
        platformSettings = platformSettingsRepository.save(platformSettings);
        return platformSettingsMapper.toDto(platformSettings);
    }

    /**
     * Update a platformSettings.
     *
     * @param platformSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    public PlatformSettingsDTO update(PlatformSettingsDTO platformSettingsDTO) {
        LOG.debug("Request to update PlatformSettings : {}", platformSettingsDTO);
        PlatformSettings platformSettings = platformSettingsMapper.toEntity(platformSettingsDTO);
        platformSettings = platformSettingsRepository.save(platformSettings);
        return platformSettingsMapper.toDto(platformSettings);
    }

    /**
     * Partially update a platformSettings.
     *
     * @param platformSettingsDTO the entity to update partially.
     * @return the persisted entity.
     */
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

    /**
     * Get all the platformSettingses.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<PlatformSettingsDTO> findAll() {
        LOG.debug("Request to get all PlatformSettingses");
        return platformSettingsRepository
            .findAll()
            .stream()
            .map(platformSettingsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one platformSettings by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PlatformSettingsDTO> findOne(Long id) {
        LOG.debug("Request to get PlatformSettings : {}", id);
        return platformSettingsRepository.findById(id).map(platformSettingsMapper::toDto);
    }

    /**
     * Delete the platformSettings by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete PlatformSettings : {}", id);
        platformSettingsRepository.deleteById(id);
    }
}
