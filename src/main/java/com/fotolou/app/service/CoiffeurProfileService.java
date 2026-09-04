package com.fotolou.app.service;

import com.fotolou.app.domain.CoiffeurProfile;
import com.fotolou.app.repository.CoiffeurProfileRepository;
import com.fotolou.app.service.dto.CoiffeurProfileDTO;
import com.fotolou.app.service.mapper.CoiffeurProfileMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.CoiffeurProfile}.
 */
@Service
@Transactional
public class CoiffeurProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(CoiffeurProfileService.class);

    private final CoiffeurProfileRepository coiffeurProfileRepository;

    private final CoiffeurProfileMapper coiffeurProfileMapper;

    public CoiffeurProfileService(CoiffeurProfileRepository coiffeurProfileRepository, CoiffeurProfileMapper coiffeurProfileMapper) {
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.coiffeurProfileMapper = coiffeurProfileMapper;
    }

    /**
     * Save a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public CoiffeurProfileDTO save(CoiffeurProfileDTO coiffeurProfileDTO) {
        LOG.debug("Request to save CoiffeurProfile : {}", coiffeurProfileDTO);
        CoiffeurProfile coiffeurProfile = coiffeurProfileMapper.toEntity(coiffeurProfileDTO);
        coiffeurProfile = coiffeurProfileRepository.save(coiffeurProfile);
        return coiffeurProfileMapper.toDto(coiffeurProfile);
    }

    /**
     * Update a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public CoiffeurProfileDTO update(CoiffeurProfileDTO coiffeurProfileDTO) {
        LOG.debug("Request to update CoiffeurProfile : {}", coiffeurProfileDTO);
        CoiffeurProfile coiffeurProfile = coiffeurProfileMapper.toEntity(coiffeurProfileDTO);
        coiffeurProfile = coiffeurProfileRepository.save(coiffeurProfile);
        return coiffeurProfileMapper.toDto(coiffeurProfile);
    }

    /**
     * Partially update a coiffeurProfile.
     *
     * @param coiffeurProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CoiffeurProfileDTO> partialUpdate(CoiffeurProfileDTO coiffeurProfileDTO) {
        LOG.debug("Request to partially update CoiffeurProfile : {}", coiffeurProfileDTO);

        return coiffeurProfileRepository
            .findById(coiffeurProfileDTO.getId())
            .map(existingCoiffeurProfile -> {
                coiffeurProfileMapper.partialUpdate(existingCoiffeurProfile, coiffeurProfileDTO);

                return existingCoiffeurProfile;
            })
            .map(coiffeurProfileRepository::save)
            .map(coiffeurProfileMapper::toDto);
    }

    /**
     * Get all the coiffeurProfiles with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CoiffeurProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return coiffeurProfileRepository.findAllWithEagerRelationships(pageable).map(coiffeurProfileMapper::toDto);
    }

    /**
     * Get one coiffeurProfile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CoiffeurProfileDTO> findOne(Long id) {
        LOG.debug("Request to get CoiffeurProfile : {}", id);
        return coiffeurProfileRepository.findOneWithEagerRelationships(id).map(coiffeurProfileMapper::toDto);
    }

    /**
     * Delete the coiffeurProfile by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CoiffeurProfile : {}", id);
        coiffeurProfileRepository.deleteById(id);
    }
}
