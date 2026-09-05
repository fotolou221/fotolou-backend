package com.fotolou.app.service;

import com.fotolou.app.service.dto.ProductDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.Product}.
 */
public interface ProductService {
    /**
     * Save a product.
     *
     * @param productDTO the entity to save.
     * @return the persisted entity.
     */
    ProductDTO save(ProductDTO productDTO);

    /**
     * Update a product.
     *
     * @param productDTO the entity to save.
     * @return the persisted entity.
     */
    ProductDTO update(ProductDTO productDTO);

    /**
     * Partially update a product.
     *
     * @param productDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProductDTO> partialUpdate(ProductDTO productDTO);

    /**
     * Get all the products with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<ProductDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProductDTO> findOne(Long id);

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a product exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
