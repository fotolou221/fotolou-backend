package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.service.criteria.BoutiqueOrderCriteria;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
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
 * Service for executing complex queries for {@link BoutiqueOrder} entities in the database.
 * The main input is a {@link BoutiqueOrderCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link BoutiqueOrderDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BoutiqueOrderQueryService extends QueryService<BoutiqueOrder> {

    private static final Logger LOG = LoggerFactory.getLogger(BoutiqueOrderQueryService.class);

    private final BoutiqueOrderRepository boutiqueOrderRepository;

    private final BoutiqueOrderMapper boutiqueOrderMapper;

    public BoutiqueOrderQueryService(BoutiqueOrderRepository boutiqueOrderRepository, BoutiqueOrderMapper boutiqueOrderMapper) {
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
    }

    /**
     * Return a {@link Page} of {@link BoutiqueOrderDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BoutiqueOrderDTO> findByCriteria(BoutiqueOrderCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<BoutiqueOrder> specification = createSpecification(criteria);
        return boutiqueOrderRepository.findAll(specification, page).map(boutiqueOrderMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BoutiqueOrderCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<BoutiqueOrder> specification = createSpecification(criteria);
        return boutiqueOrderRepository.count(specification);
    }

    /**
     * Function to convert {@link BoutiqueOrderCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BoutiqueOrder> createSpecification(BoutiqueOrderCriteria criteria) {
        Specification<BoutiqueOrder> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(BoutiqueOrder_.user, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), BoutiqueOrder_.id),
                    buildStringSpecification(criteria.getOrderNumber(), BoutiqueOrder_.orderNumber),
                    buildRangeSpecification(criteria.getSubtotal(), BoutiqueOrder_.subtotal),
                    buildRangeSpecification(criteria.getDeliveryFee(), BoutiqueOrder_.deliveryFee),
                    buildRangeSpecification(criteria.getTotalPrice(), BoutiqueOrder_.totalPrice),
                    buildSpecification(criteria.getStatus(), BoutiqueOrder_.status),
                    buildSpecification(criteria.getOrderType(), BoutiqueOrder_.orderType),
                    buildStringSpecification(criteria.getDeliveryAddress(), BoutiqueOrder_.deliveryAddress),
                    buildStringSpecification(criteria.getDeliveryDistrict(), BoutiqueOrder_.deliveryDistrict),
                    buildStringSpecification(criteria.getCustomerName(), BoutiqueOrder_.customerName),
                    buildStringSpecification(criteria.getCustomerPhone(), BoutiqueOrder_.customerPhone),
                    buildRangeSpecification(criteria.getCreatedDate(), BoutiqueOrder_.createdDate),
                    buildRangeSpecification(criteria.getLastModifiedDate(), BoutiqueOrder_.lastModifiedDate),
                    buildSpecification(criteria.getItemsId(), root -> root.join(BoutiqueOrder_.itemses, JoinType.LEFT).get(OrderItem_.id)),
                    buildSpecification(criteria.getUserId(), root -> root.join(BoutiqueOrder_.user, JoinType.LEFT).get(User_.id))
                )
            );
        }
        return specification;
    }
}
