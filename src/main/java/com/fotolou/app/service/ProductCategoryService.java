package com.fotolou.app.service;

import com.fotolou.app.service.dto.ProductCategoryDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.ProductCategory}.
 */
public interface ProductCategoryService {
    /**
     * Save a productCategory.
     *
     * @param productCategoryDTO the entity to save.
     * @return the persisted entity.
     */
    ProductCategoryDTO save(ProductCategoryDTO productCategoryDTO);

    /**
     * Update a productCategory.
     *
     * @param productCategoryDTO the entity to save.
     * @return the persisted entity.
     */
    ProductCategoryDTO update(ProductCategoryDTO productCategoryDTO);

    /**
     * Partially update a productCategory.
     *
     * @param productCategoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProductCategoryDTO> partialUpdate(ProductCategoryDTO productCategoryDTO);

    /**
     * Get all the productCategories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProductCategoryDTO> findAll(Pageable pageable);

    /**
     * Get one productCategory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProductCategoryDTO> findOne(Long id);

    /**
     * Delete the productCategory by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a productCategory exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
