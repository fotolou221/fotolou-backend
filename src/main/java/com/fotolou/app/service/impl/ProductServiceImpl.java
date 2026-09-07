package com.fotolou.app.service.impl;

import com.fotolou.app.domain.Product;
import com.fotolou.app.domain.ProductImage;
import com.fotolou.app.repository.ProductImageRepository;
import com.fotolou.app.repository.ProductRepository;
import com.fotolou.app.service.ProductService;
import com.fotolou.app.service.dto.ProductDTO;
import com.fotolou.app.service.mapper.ProductMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Product}.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;

    public ProductServiceImpl(
        ProductRepository productRepository,
        ProductMapper productMapper,
        ProductImageRepository productImageRepository
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productImageRepository = productImageRepository;
    }

    private void saveProductImages(Product product, List<String> images) {
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String url = images.get(i);
                if (url != null && !url.isBlank()) {
                    ProductImage img = new ProductImage();
                    img.setImageUrl(url);
                    img.setSortOrder(i);
                    img.setProduct(product);
                    productImageRepository.save(img);
                }
            }
        }
    }

    private void attachImages(ProductDTO dto, Long productId) {
        if (dto != null && productId != null) {
            List<String> images = productImageRepository
                .findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();
            dto.setImages(images);
        }
    }

    @Override
    public ProductDTO save(ProductDTO productDTO) {
        LOG.debug("Request to save Product : {}", productDTO);
        Product product = productMapper.toEntity(productDTO);
        product = productRepository.save(product);
        saveProductImages(product, productDTO.getImages());
        ProductDTO result = productMapper.toDto(product);
        attachImages(result, product.getId());
        return result;
    }

    @Override
    public ProductDTO update(ProductDTO productDTO) {
        LOG.debug("Request to update Product : {}", productDTO);
        Product product = productMapper.toEntity(productDTO);
        product = productRepository.save(product);
        if (productDTO.getImages() != null) {
            productImageRepository.deleteByProductId(product.getId());
            saveProductImages(product, productDTO.getImages());
        }
        ProductDTO result = productMapper.toDto(product);
        attachImages(result, product.getId());
        return result;
    }

    @Override
    public Optional<ProductDTO> partialUpdate(ProductDTO productDTO) {
        LOG.debug("Request to partially update Product : {}", productDTO);

        return productRepository
            .findById(productDTO.getId())
            .map(existingProduct -> {
                productMapper.partialUpdate(existingProduct, productDTO);
                return existingProduct;
            })
            .map(productRepository::save)
            .map(product -> {
                if (productDTO.getImages() != null) {
                    productImageRepository.deleteByProductId(product.getId());
                    saveProductImages(product, productDTO.getImages());
                }
                ProductDTO result = productMapper.toDto(product);
                attachImages(result, product.getId());
                return result;
            });
    }

    @Override
    public Page<ProductDTO> findAllWithEagerRelationships(Pageable pageable) {
        return productRepository.findAllWithEagerRelationships(pageable).map(product -> {
            ProductDTO dto = productMapper.toDto(product);
            attachImages(dto, product.getId());
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOne(Long id) {
        LOG.debug("Request to get Product : {}", id);
        return productRepository.findOneWithEagerRelationships(id).map(product -> {
            ProductDTO dto = productMapper.toDto(product);
            attachImages(dto, product.getId());
            return dto;
        });
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Product : {}", id);
        productImageRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
}
