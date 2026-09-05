package com.fotolou.app.service.impl;

import com.fotolou.app.domain.CoiffeurProfile;
import com.fotolou.app.repository.CoiffeurProfileRepository;
import com.fotolou.app.service.CoiffeurProfileService;
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
public class CoiffeurProfileServiceImpl implements CoiffeurProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(CoiffeurProfileServiceImpl.class);

    private final CoiffeurProfileRepository coiffeurProfileRepository;
    private final CoiffeurProfileMapper coiffeurProfileMapper;

    public CoiffeurProfileServiceImpl(CoiffeurProfileRepository coiffeurProfileRepository, CoiffeurProfileMapper coiffeurProfileMapper) {
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.coiffeurProfileMapper = coiffeurProfileMapper;
    }

    @Override
    public CoiffeurProfileDTO save(CoiffeurProfileDTO coiffeurProfileDTO) {
        LOG.debug("Request to save CoiffeurProfile : {}", coiffeurProfileDTO);
        CoiffeurProfile coiffeurProfile = coiffeurProfileMapper.toEntity(coiffeurProfileDTO);
        coiffeurProfile = coiffeurProfileRepository.save(coiffeurProfile);
        return coiffeurProfileMapper.toDto(coiffeurProfile);
    }

    @Override
    public CoiffeurProfileDTO update(CoiffeurProfileDTO coiffeurProfileDTO) {
        LOG.debug("Request to update CoiffeurProfile : {}", coiffeurProfileDTO);
        CoiffeurProfile coiffeurProfile = coiffeurProfileMapper.toEntity(coiffeurProfileDTO);
        coiffeurProfile = coiffeurProfileRepository.save(coiffeurProfile);
        return coiffeurProfileMapper.toDto(coiffeurProfile);
    }

    @Override
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

    @Override
    public Page<CoiffeurProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return coiffeurProfileRepository.findAllWithEagerRelationships(pageable).map(coiffeurProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoiffeurProfileDTO> findOne(Long id) {
        LOG.debug("Request to get CoiffeurProfile : {}", id);
        return coiffeurProfileRepository.findOneWithEagerRelationships(id).map(coiffeurProfileMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete CoiffeurProfile : {}", id);
        coiffeurProfileRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return coiffeurProfileRepository.existsById(id);
    }
}
