package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.CoiffeurProfile;
import com.fotolou.app.repository.CoiffeurProfileRepository;
import com.fotolou.app.service.criteria.CoiffeurProfileCriteria;
import com.fotolou.app.service.dto.CoiffeurProfileDTO;
import com.fotolou.app.service.mapper.CoiffeurProfileMapper;
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
 * Service for executing complex queries for {@link CoiffeurProfile} entities in the database.
 * The main input is a {@link CoiffeurProfileCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CoiffeurProfileDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CoiffeurProfileQueryService extends QueryService<CoiffeurProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(CoiffeurProfileQueryService.class);

    private final CoiffeurProfileRepository coiffeurProfileRepository;

    private final CoiffeurProfileMapper coiffeurProfileMapper;

    public CoiffeurProfileQueryService(CoiffeurProfileRepository coiffeurProfileRepository, CoiffeurProfileMapper coiffeurProfileMapper) {
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.coiffeurProfileMapper = coiffeurProfileMapper;
    }

    /**
     * Return a {@link Page} of {@link CoiffeurProfileDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CoiffeurProfileDTO> findByCriteria(CoiffeurProfileCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CoiffeurProfile> specification = createSpecification(criteria);
        return coiffeurProfileRepository.findAll(specification, page).map(coiffeurProfileMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CoiffeurProfileCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CoiffeurProfile> specification = createSpecification(criteria);
        return coiffeurProfileRepository.count(specification);
    }

    /**
     * Function to convert {@link CoiffeurProfileCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CoiffeurProfile> createSpecification(CoiffeurProfileCriteria criteria) {
        Specification<CoiffeurProfile> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(CoiffeurProfile_.user, JoinType.LEFT);
                root.fetch(CoiffeurProfile_.salon, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), CoiffeurProfile_.id),
                    buildStringSpecification(criteria.getName(), CoiffeurProfile_.name),
                    buildStringSpecification(criteria.getPhone(), CoiffeurProfile_.phone),
                    buildStringSpecification(criteria.getSpecialty(), CoiffeurProfile_.specialty),
                    buildSpecification(criteria.getActive(), CoiffeurProfile_.active),
                    buildStringSpecification(criteria.getAvatarUrl(), CoiffeurProfile_.avatarUrl),
                    buildRangeSpecification(criteria.getTicketsServedCount(), CoiffeurProfile_.ticketsServedCount),
                    buildRangeSpecification(criteria.getCreatedDate(), CoiffeurProfile_.createdDate),
                    buildSpecification(criteria.getUserId(), root -> root.join(CoiffeurProfile_.user, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getSalonId(), root -> root.join(CoiffeurProfile_.salon, JoinType.LEFT).get(Salon_.id))
                )
            );
        }
        return specification;
    }
}
