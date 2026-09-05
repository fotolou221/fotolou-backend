package com.fotolou.app.service.impl;

import com.fotolou.app.domain.SalonAction;
import com.fotolou.app.repository.SalonActionRepository;
import com.fotolou.app.service.SalonActionService;
import com.fotolou.app.service.dto.SalonActionDTO;
import com.fotolou.app.service.mapper.SalonActionMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.SalonAction}.
 */
@Service
@Transactional
public class SalonActionServiceImpl implements SalonActionService {

    private static final Logger LOG = LoggerFactory.getLogger(SalonActionServiceImpl.class);

    private final SalonActionRepository salonActionRepository;
    private final SalonActionMapper salonActionMapper;

    public SalonActionServiceImpl(SalonActionRepository salonActionRepository, SalonActionMapper salonActionMapper) {
        this.salonActionRepository = salonActionRepository;
        this.salonActionMapper = salonActionMapper;
    }

    @Override
    public SalonActionDTO save(SalonActionDTO salonActionDTO) {
        LOG.debug("Request to save SalonAction : {}", salonActionDTO);
        SalonAction salonAction = salonActionMapper.toEntity(salonActionDTO);
        salonAction = salonActionRepository.save(salonAction);
        return salonActionMapper.toDto(salonAction);
    }

    @Override
    public SalonActionDTO update(SalonActionDTO salonActionDTO) {
        LOG.debug("Request to update SalonAction : {}", salonActionDTO);
        SalonAction salonAction = salonActionMapper.toEntity(salonActionDTO);
        salonAction = salonActionRepository.save(salonAction);
        return salonActionMapper.toDto(salonAction);
    }

    @Override
    public Optional<SalonActionDTO> partialUpdate(SalonActionDTO salonActionDTO) {
        LOG.debug("Request to partially update SalonAction : {}", salonActionDTO);

        return salonActionRepository
            .findById(salonActionDTO.getId())
            .map(existingSalonAction -> {
                salonActionMapper.partialUpdate(existingSalonAction, salonActionDTO);
                return existingSalonAction;
            })
            .map(salonActionRepository::save)
            .map(salonActionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalonActionDTO> findAll() {
        LOG.debug("Request to get all SalonActions");
        return salonActionRepository.findAll().stream().map(salonActionMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Page<SalonActionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return salonActionRepository.findAllWithEagerRelationships(pageable).map(salonActionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalonActionDTO> findOne(Long id) {
        LOG.debug("Request to get SalonAction : {}", id);
        return salonActionRepository.findOneWithEagerRelationships(id).map(salonActionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete SalonAction : {}", id);
        salonActionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return salonActionRepository.existsById(id);
    }
}
