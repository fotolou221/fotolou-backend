package com.fotolou.app.service;

import com.fotolou.app.service.dto.OrderItemDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.OrderItem}.
 */
public interface OrderItemService {
    /**
     * Save a orderItem.
     *
     * @param orderItemDTO the entity to save.
     * @return the persisted entity.
     */
    OrderItemDTO save(OrderItemDTO orderItemDTO);

    /**
     * Update a orderItem.
     *
     * @param orderItemDTO the entity to save.
     * @return the persisted entity.
     */
    OrderItemDTO update(OrderItemDTO orderItemDTO);

    /**
     * Partially update a orderItem.
     *
     * @param orderItemDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<OrderItemDTO> partialUpdate(OrderItemDTO orderItemDTO);

    /**
     * Get all the orderItems.
     *
     * @return the list of entities.
     */
    List<OrderItemDTO> findAll();

    /**
     * Get all the orderItems with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<OrderItemDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one orderItem by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<OrderItemDTO> findOne(Long id);

    /**
     * Delete the orderItem by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if an orderItem exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
