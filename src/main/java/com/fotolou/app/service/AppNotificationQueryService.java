package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.repository.AppNotificationRepository;
import com.fotolou.app.service.criteria.AppNotificationCriteria;
import com.fotolou.app.service.dto.AppNotificationDTO;
import com.fotolou.app.service.mapper.AppNotificationMapper;
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
 * Service for executing complex queries for {@link AppNotification} entities in the database.
 * The main input is a {@link AppNotificationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AppNotificationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AppNotificationQueryService extends QueryService<AppNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(AppNotificationQueryService.class);

    private final AppNotificationRepository appNotificationRepository;

    private final AppNotificationMapper appNotificationMapper;

    public AppNotificationQueryService(AppNotificationRepository appNotificationRepository, AppNotificationMapper appNotificationMapper) {
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
    }

    /**
     * Return a {@link Page} of {@link AppNotificationDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AppNotificationDTO> findByCriteria(AppNotificationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AppNotification> specification = createSpecification(criteria);
        return appNotificationRepository.findAll(specification, page).map(appNotificationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AppNotificationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AppNotification> specification = createSpecification(criteria);
        return appNotificationRepository.count(specification);
    }

    /**
     * Function to convert {@link AppNotificationCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AppNotification> createSpecification(AppNotificationCriteria criteria) {
        Specification<AppNotification> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(AppNotification_.user, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), AppNotification_.id),
                    buildStringSpecification(criteria.getTitle(), AppNotification_.title),
                    buildSpecification(criteria.getType(), AppNotification_.type),
                    buildSpecification(criteria.getRecipientRole(), AppNotification_.recipientRole),
                    buildSpecification(criteria.getIsRead(), AppNotification_.isRead),
                    buildStringSpecification(criteria.getTargetRoute(), AppNotification_.targetRoute),
                    buildRangeSpecification(criteria.getCreatedDate(), AppNotification_.createdDate),
                    buildSpecification(criteria.getUserId(), root -> root.join(AppNotification_.user, JoinType.LEFT).get(User_.id))
                )
            );
        }
        return specification;
    }
}
