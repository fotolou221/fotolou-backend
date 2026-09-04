package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.Salon;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.service.criteria.SalonCriteria;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.SalonMapper;
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
 * Service for executing complex queries for {@link Salon} entities in the database.
 * The main input is a {@link SalonCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SalonDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SalonQueryService extends QueryService<Salon> {

    private static final Logger LOG = LoggerFactory.getLogger(SalonQueryService.class);

    private final SalonRepository salonRepository;

    private final SalonMapper salonMapper;

    public SalonQueryService(SalonRepository salonRepository, SalonMapper salonMapper) {
        this.salonRepository = salonRepository;
        this.salonMapper = salonMapper;
    }

    /**
     * Return a {@link Page} of {@link SalonDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SalonDTO> findByCriteria(SalonCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Salon> specification = createSpecification(criteria);
        return salonRepository.findAll(specification, page).map(salonMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SalonCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Salon> specification = createSpecification(criteria);
        return salonRepository.count(specification);
    }

    /**
     * Function to convert {@link SalonCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Salon> createSpecification(SalonCriteria criteria) {
        Specification<Salon> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Salon_.id),
                    buildStringSpecification(criteria.getName(), Salon_.name),
                    buildStringSpecification(criteria.getSlug(), Salon_.slug),
                    buildStringSpecification(criteria.getLocation(), Salon_.location),
                    buildStringSpecification(criteria.getDistrict(), Salon_.district),
                    buildStringSpecification(criteria.getAddress(), Salon_.address),
                    buildSpecification(criteria.getStatus(), Salon_.status),
                    buildStringSpecification(criteria.getPhone(), Salon_.phone),
                    buildStringSpecification(criteria.getOpeningHours(), Salon_.openingHours),
                    buildRangeSpecification(criteria.getEstimatedWaitMinutes(), Salon_.estimatedWaitMinutes),
                    buildRangeSpecification(criteria.getPeopleWaiting(), Salon_.peopleWaiting),
                    buildStringSpecification(criteria.getAvatarUrl(), Salon_.avatarUrl),
                    buildStringSpecification(criteria.getCoverUrl(), Salon_.coverUrl),
                    buildRangeSpecification(criteria.getLatitude(), Salon_.latitude),
                    buildRangeSpecification(criteria.getLongitude(), Salon_.longitude),
                    buildSpecification(criteria.getActive(), Salon_.active),
                    buildRangeSpecification(criteria.getCreatedDate(), Salon_.createdDate),
                    buildRangeSpecification(criteria.getLastModifiedDate(), Salon_.lastModifiedDate),
                    buildSpecification(criteria.getActionsId(), root -> root.join(Salon_.actionses, JoinType.LEFT).get(SalonAction_.id)),
                    buildSpecification(criteria.getCoiffeursId(), root ->
                        root.join(Salon_.coiffeurses, JoinType.LEFT).get(CoiffeurProfile_.id)
                    ),
                    buildSpecification(criteria.getTicketsId(), root -> root.join(Salon_.ticketses, JoinType.LEFT).get(Ticket_.id))
                )
            );
        }
        return specification;
    }
}
