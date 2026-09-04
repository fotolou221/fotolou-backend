package com.fotolou.app.service;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.BoutiqueOrder}.
 */
@Service
@Transactional
public class BoutiqueOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BoutiqueOrderService.class);

    private final BoutiqueOrderRepository boutiqueOrderRepository;

    private final BoutiqueOrderMapper boutiqueOrderMapper;

    public BoutiqueOrderService(BoutiqueOrderRepository boutiqueOrderRepository, BoutiqueOrderMapper boutiqueOrderMapper) {
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
    }

    /**
     * Save a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to save.
     * @return the persisted entity.
     */
    public BoutiqueOrderDTO save(BoutiqueOrderDTO boutiqueOrderDTO) {
        LOG.debug("Request to save BoutiqueOrder : {}", boutiqueOrderDTO);
        BoutiqueOrder boutiqueOrder = boutiqueOrderMapper.toEntity(boutiqueOrderDTO);
        boutiqueOrder = boutiqueOrderRepository.save(boutiqueOrder);
        return boutiqueOrderMapper.toDto(boutiqueOrder);
    }

    /**
     * Update a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to save.
     * @return the persisted entity.
     */
    public BoutiqueOrderDTO update(BoutiqueOrderDTO boutiqueOrderDTO) {
        LOG.debug("Request to update BoutiqueOrder : {}", boutiqueOrderDTO);
        BoutiqueOrder boutiqueOrder = boutiqueOrderMapper.toEntity(boutiqueOrderDTO);
        boutiqueOrder = boutiqueOrderRepository.save(boutiqueOrder);
        return boutiqueOrderMapper.toDto(boutiqueOrder);
    }

    /**
     * Partially update a boutiqueOrder.
     *
     * @param boutiqueOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BoutiqueOrderDTO> partialUpdate(BoutiqueOrderDTO boutiqueOrderDTO) {
        LOG.debug("Request to partially update BoutiqueOrder : {}", boutiqueOrderDTO);

        return boutiqueOrderRepository
            .findById(boutiqueOrderDTO.getId())
            .map(existingBoutiqueOrder -> {
                boutiqueOrderMapper.partialUpdate(existingBoutiqueOrder, boutiqueOrderDTO);

                return existingBoutiqueOrder;
            })
            .map(boutiqueOrderRepository::save)
            .map(boutiqueOrderMapper::toDto);
    }

    /**
     * Get one boutiqueOrder by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BoutiqueOrderDTO> findOne(Long id) {
        LOG.debug("Request to get BoutiqueOrder : {}", id);
        return boutiqueOrderRepository.findById(id).map(boutiqueOrderMapper::toDto);
    }

    /**
     * Delete the boutiqueOrder by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete BoutiqueOrder : {}", id);
        boutiqueOrderRepository.deleteById(id);
    }
}
