package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.Product;
import com.fotolou.app.repository.ProductRepository;
import com.fotolou.app.service.criteria.ProductCriteria;
import com.fotolou.app.service.dto.ProductDTO;
import com.fotolou.app.service.mapper.ProductMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Product} entities in the database.
 * The main input is a {@link ProductCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ProductDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService extends QueryService<Product> {

    private static final Logger LOG = LoggerFactory.getLogger(ProductQueryService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final com.fotolou.app.repository.ProductImageRepository productImageRepository;

    public ProductQueryService(
        ProductRepository productRepository,
        ProductMapper productMapper,
        com.fotolou.app.repository.ProductImageRepository productImageRepository
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productImageRepository = productImageRepository;
    }

    /**
     * Return a {@link Page} of {@link ProductDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCriteria(ProductCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Product> specification = createSpecification(criteria);
        return productRepository.findAll(specification, page).map(product -> {
            ProductDTO dto = productMapper.toDto(product);
            java.util.List<String> images = productImageRepository
                .findByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(com.fotolou.app.domain.ProductImage::getImageUrl)
                .toList();
            dto.setImages(images);
            return dto;
        });
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ProductCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Product> specification = createSpecification(criteria);
        return productRepository.count(specification);
    }

    /**
     * Function to convert {@link ProductCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Product> createSpecification(ProductCriteria criteria) {
        Specification<Product> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Product_.category, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Product_.id),
                    buildStringSpecification(criteria.getBrand(), Product_.brand),
                    buildStringSpecification(criteria.getTitle(), Product_.title),
                    buildRangeSpecification(criteria.getPrice(), Product_.price),
                    buildRangeSpecification(criteria.getOldPrice(), Product_.oldPrice),
                    buildRangeSpecification(criteria.getRating(), Product_.rating),
                    buildSpecification(criteria.getInStock(), Product_.inStock),
                    buildRangeSpecification(criteria.getCreatedDate(), Product_.createdDate),
                    buildRangeSpecification(criteria.getLastModifiedDate(), Product_.lastModifiedDate),
                    buildSpecification(criteria.getImagesId(), root -> root.join(Product_.imageses, JoinType.LEFT).get(ProductImage_.id)),
                    buildSpecification(criteria.getCategoryId(), root ->
                        root.join(Product_.category, JoinType.LEFT).get(ProductCategory_.id)
                    )
                )
            );
        }
        return specification;
    }
}
