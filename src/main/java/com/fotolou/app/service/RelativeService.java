package com.fotolou.app.service;

import com.fotolou.app.domain.Relative;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.mapper.RelativeMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Relative}.
 */
@Service
@Transactional
public class RelativeService {

    private static final Logger LOG = LoggerFactory.getLogger(RelativeService.class);

    private final RelativeRepository relativeRepository;

    private final RelativeMapper relativeMapper;

    public RelativeService(RelativeRepository relativeRepository, RelativeMapper relativeMapper) {
        this.relativeRepository = relativeRepository;
        this.relativeMapper = relativeMapper;
    }

    /**
     * Save a relative.
     *
     * @param relativeDTO the entity to save.
     * @return the persisted entity.
     */
    public RelativeDTO save(RelativeDTO relativeDTO) {
        LOG.debug("Request to save Relative : {}", relativeDTO);
        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    /**
     * Update a relative.
     *
     * @param relativeDTO the entity to save.
     * @return the persisted entity.
     */
    public RelativeDTO update(RelativeDTO relativeDTO) {
        LOG.debug("Request to update Relative : {}", relativeDTO);
        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    /**
     * Partially update a relative.
     *
     * @param relativeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RelativeDTO> partialUpdate(RelativeDTO relativeDTO) {
        LOG.debug("Request to partially update Relative : {}", relativeDTO);

        return relativeRepository
            .findById(relativeDTO.getId())
            .map(existingRelative -> {
                relativeMapper.partialUpdate(existingRelative, relativeDTO);

                return existingRelative;
            })
            .map(relativeRepository::save)
            .map(relativeMapper::toDto);
    }

    /**
     * Get all the relatives.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RelativeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Relatives");
        return relativeRepository.findAll(pageable).map(relativeMapper::toDto);
    }

    /**
     * Get one relative by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RelativeDTO> findOne(Long id) {
        LOG.debug("Request to get Relative : {}", id);
        return relativeRepository.findById(id).map(relativeMapper::toDto);
    }

    /**
     * Delete the relative by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Relative : {}", id);
        relativeRepository.deleteById(id);
    }
}
