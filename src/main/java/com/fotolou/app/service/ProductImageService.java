package com.fotolou.app.service;

import com.fotolou.app.service.dto.ProductImageDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.ProductImage}.
 */
public interface ProductImageService {
    /**
     * Save a productImage.
     *
     * @param productImageDTO the entity to save.
     * @return the persisted entity.
     */
    ProductImageDTO save(ProductImageDTO productImageDTO);

    /**
     * Update a productImage.
     *
     * @param productImageDTO the entity to save.
     * @return the persisted entity.
     */
    ProductImageDTO update(ProductImageDTO productImageDTO);

    /**
     * Partially update a productImage.
     *
     * @param productImageDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProductImageDTO> partialUpdate(ProductImageDTO productImageDTO);

    /**
     * Get all the productImages.
     *
     * @return the list of entities.
     */
    List<ProductImageDTO> findAll();

    /**
     * Get all the productImages with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<ProductImageDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one productImage by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProductImageDTO> findOne(Long id);

    /**
     * Delete the productImage by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a productImage exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
