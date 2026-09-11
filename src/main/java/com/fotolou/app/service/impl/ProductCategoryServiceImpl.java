package com.fotolou.app.service.impl;

import com.fotolou.app.domain.ProductCategory;
import com.fotolou.app.repository.ProductCategoryRepository;
import com.fotolou.app.repository.ProductRepository;
import com.fotolou.app.service.ProductCategoryService;
import com.fotolou.app.service.custom.realtime.RealtimeEventService;
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
    private final ProductRepository productRepository;
    private final RealtimeEventService realtimeEventService;

    public ProductCategoryServiceImpl(
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryMapper productCategoryMapper,
        ProductRepository productRepository,
        RealtimeEventService realtimeEventService
    ) {
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
        this.productRepository = productRepository;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public ProductCategoryDTO save(ProductCategoryDTO productCategoryDTO) {
        LOG.debug("Request to save ProductCategory : {}", productCategoryDTO);
        ProductCategory productCategory = productCategoryMapper.toEntity(productCategoryDTO);
        productCategory = productCategoryRepository.save(productCategory);
        ProductCategoryDTO result = productCategoryMapper.toDto(productCategory);
        broadcast("CATEGORY_UPDATED", result);
        return result;
    }

    @Override
    public ProductCategoryDTO update(ProductCategoryDTO productCategoryDTO) {
        LOG.debug("Request to update ProductCategory : {}", productCategoryDTO);
        ProductCategory productCategory = productCategoryMapper.toEntity(productCategoryDTO);
        productCategory = productCategoryRepository.save(productCategory);
        ProductCategoryDTO result = productCategoryMapper.toDto(productCategory);
        broadcast("CATEGORY_UPDATED", result);
        return result;
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
            .map(productCategoryMapper::toDto)
            .map(dto -> {
                broadcast("CATEGORY_UPDATED", dto);
                return dto;
            });
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
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalStateException(
                "Cette catégorie contient des produits. Déplacez ou supprimez d'abord ces produits avant de supprimer la catégorie."
            );
        }
        productCategoryRepository.deleteById(id);
        broadcast("CATEGORY_DELETED", java.util.Map.of("id", id));
    }

    private void broadcast(String event, Object payload) {
        try {
            realtimeEventService.broadcast(event, payload);
        } catch (Exception e) {
            LOG.warn("Diffusion temps réel {} impossible : {}", event, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productCategoryRepository.existsById(id);
    }
}
