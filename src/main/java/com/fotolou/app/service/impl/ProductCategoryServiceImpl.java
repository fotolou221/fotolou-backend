package com.fotolou.app.service.impl;

import com.fotolou.app.domain.ProductCategory;
import com.fotolou.app.repository.ProductCategoryRepository;
import com.fotolou.app.service.ProductCategoryService;
import com.fotolou.app.service.dto.ProductCategoryDTO;
import com.fotolou.app.service.mapper.ProductCategoryMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.ProductCategory}.
 */
@Service
@Transactional
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCategoryServiceImpl.class);

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository, ProductCategoryMapper productCategoryMapper) {
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
    }

    @Override
    public ProductCategoryDTO save(ProductCategoryDTO productCategoryDTO) {
        LOG.debug("Request to save ProductCategory : {}", productCategoryDTO);
        ProductCategory productCategory = productCategoryMapper.toEntity(productCategoryDTO);
        productCategory = productCategoryRepository.save(productCategory);
        return productCategoryMapper.toDto(productCategory);
    }

    @Override
    public ProductCategoryDTO update(ProductCategoryDTO productCategoryDTO) {
        LOG.debug("Request to update ProductCategory : {}", productCategoryDTO);
        ProductCategory productCategory = productCategoryMapper.toEntity(productCategoryDTO);
        productCategory = productCategoryRepository.save(productCategory);
        return productCategoryMapper.toDto(productCategory);
    }

    @Override
    public Optional<ProductCategoryDTO> partialUpdate(ProductCategoryDTO productCategoryDTO) {
        LOG.debug("Request to partially update ProductCategory : {}", productCategoryDTO);

        return productCategoryRepository
            .findById(productCategoryDTO.getId())
            .map(existingProductCategory -> {
                productCategoryMapper.partialUpdate(existingProductCategory, productCategoryDTO);
                return existingProductCategory;
            })
            .map(productCategoryRepository::save)
            .map(productCategoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCategoryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ProductCategories");
        return productCategoryRepository.findAll(pageable).map(productCategoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductCategoryDTO> findOne(Long id) {
        LOG.debug("Request to get ProductCategory : {}", id);
        return productCategoryRepository.findById(id).map(productCategoryMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ProductCategory : {}", id);
        productCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productCategoryRepository.existsById(id);
    }
}
