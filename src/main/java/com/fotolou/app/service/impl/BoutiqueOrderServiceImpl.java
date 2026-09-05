package com.fotolou.app.service.impl;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.service.BoutiqueOrderService;
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
public class BoutiqueOrderServiceImpl implements BoutiqueOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BoutiqueOrderServiceImpl.class);

    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final BoutiqueOrderMapper boutiqueOrderMapper;

    public BoutiqueOrderServiceImpl(BoutiqueOrderRepository boutiqueOrderRepository, BoutiqueOrderMapper boutiqueOrderMapper) {
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
    }

    @Override
    public BoutiqueOrderDTO save(BoutiqueOrderDTO boutiqueOrderDTO) {
        LOG.debug("Request to save BoutiqueOrder : {}", boutiqueOrderDTO);
        BoutiqueOrder boutiqueOrder = boutiqueOrderMapper.toEntity(boutiqueOrderDTO);
        boutiqueOrder = boutiqueOrderRepository.save(boutiqueOrder);
        return boutiqueOrderMapper.toDto(boutiqueOrder);
    }

    @Override
    public BoutiqueOrderDTO update(BoutiqueOrderDTO boutiqueOrderDTO) {
        LOG.debug("Request to update BoutiqueOrder : {}", boutiqueOrderDTO);
        BoutiqueOrder boutiqueOrder = boutiqueOrderMapper.toEntity(boutiqueOrderDTO);
        boutiqueOrder = boutiqueOrderRepository.save(boutiqueOrder);
        return boutiqueOrderMapper.toDto(boutiqueOrder);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Optional<BoutiqueOrderDTO> findOne(Long id) {
        LOG.debug("Request to get BoutiqueOrder : {}", id);
        return boutiqueOrderRepository.findById(id).map(boutiqueOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BoutiqueOrder : {}", id);
        boutiqueOrderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return boutiqueOrderRepository.existsById(id);
    }
}
