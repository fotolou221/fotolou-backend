package com.fotolou.app.service.impl;

import com.fotolou.app.domain.OrderItem;
import com.fotolou.app.repository.OrderItemRepository;
import com.fotolou.app.service.OrderItemService;
import com.fotolou.app.service.dto.OrderItemDTO;
import com.fotolou.app.service.mapper.OrderItemMapper;
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
 * Service Implementation for managing {@link com.fotolou.app.domain.OrderItem}.
 */
@Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderItemServiceImpl.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public OrderItemDTO save(OrderItemDTO orderItemDTO) {
        LOG.debug("Request to save OrderItem : {}", orderItemDTO);
        OrderItem orderItem = orderItemMapper.toEntity(orderItemDTO);
        orderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    public OrderItemDTO update(OrderItemDTO orderItemDTO) {
        LOG.debug("Request to update OrderItem : {}", orderItemDTO);
        OrderItem orderItem = orderItemMapper.toEntity(orderItemDTO);
        orderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    public Optional<OrderItemDTO> partialUpdate(OrderItemDTO orderItemDTO) {
        LOG.debug("Request to partially update OrderItem : {}", orderItemDTO);

        return orderItemRepository
            .findById(orderItemDTO.getId())
            .map(existingOrderItem -> {
                orderItemMapper.partialUpdate(existingOrderItem, orderItemDTO);
                return existingOrderItem;
            })
            .map(orderItemRepository::save)
            .map(orderItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemDTO> findAll() {
        LOG.debug("Request to get all OrderItems");
        return orderItemRepository.findAll().stream().map(orderItemMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Page<OrderItemDTO> findAllWithEagerRelationships(Pageable pageable) {
        return orderItemRepository.findAllWithEagerRelationships(pageable).map(orderItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderItemDTO> findOne(Long id) {
        LOG.debug("Request to get OrderItem : {}", id);
        return orderItemRepository.findOneWithEagerRelationships(id).map(orderItemMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete OrderItem : {}", id);
        orderItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderItemRepository.existsById(id);
    }
}
