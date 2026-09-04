package com.fotolou.app.service;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.SalonMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Salon}.
 */
@Service
@Transactional
public class SalonService {

    private static final Logger LOG = LoggerFactory.getLogger(SalonService.class);

    private final SalonRepository salonRepository;

    private final SalonMapper salonMapper;

    public SalonService(SalonRepository salonRepository, SalonMapper salonMapper) {
        this.salonRepository = salonRepository;
        this.salonMapper = salonMapper;
    }

    /**
     * Save a salon.
     *
     * @param salonDTO the entity to save.
     * @return the persisted entity.
     */
    public SalonDTO save(SalonDTO salonDTO) {
        LOG.debug("Request to save Salon : {}", salonDTO);
        Salon salon = salonMapper.toEntity(salonDTO);
        salon = salonRepository.save(salon);
        return salonMapper.toDto(salon);
    }

    /**
     * Update a salon.
     *
     * @param salonDTO the entity to save.
     * @return the persisted entity.
     */
    public SalonDTO update(SalonDTO salonDTO) {
        LOG.debug("Request to update Salon : {}", salonDTO);
        Salon salon = salonMapper.toEntity(salonDTO);
        salon = salonRepository.save(salon);
        return salonMapper.toDto(salon);
    }

    /**
     * Partially update a salon.
     *
     * @param salonDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SalonDTO> partialUpdate(SalonDTO salonDTO) {
        LOG.debug("Request to partially update Salon : {}", salonDTO);

        return salonRepository
            .findById(salonDTO.getId())
            .map(existingSalon -> {
                salonMapper.partialUpdate(existingSalon, salonDTO);

                return existingSalon;
            })
            .map(salonRepository::save)
            .map(salonMapper::toDto);
    }

    /**
     * Get one salon by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SalonDTO> findOne(Long id) {
        LOG.debug("Request to get Salon : {}", id);
        return salonRepository.findById(id).map(salonMapper::toDto);
    }

    /**
     * Delete the salon by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Salon : {}", id);
        salonRepository.deleteById(id);
    }
}
