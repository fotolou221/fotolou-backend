package com.fotolou.app.service;

import com.fotolou.app.domain.*; // for static metamodels
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.service.criteria.TicketCriteria;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
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
 * Service for executing complex queries for {@link Ticket} entities in the database.
 * The main input is a {@link TicketCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TicketDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TicketQueryService extends QueryService<Ticket> {

    private static final Logger LOG = LoggerFactory.getLogger(TicketQueryService.class);

    private final TicketRepository ticketRepository;

    private final TicketMapper ticketMapper;

    public TicketQueryService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    /**
     * Return a {@link Page} of {@link TicketDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TicketDTO> findByCriteria(TicketCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Ticket> specification = createSpecification(criteria);
        return ticketRepository.findAll(specification, page).map(ticketMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TicketCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Ticket> specification = createSpecification(criteria);
        return ticketRepository.count(specification);
    }

    /**
     * Function to convert {@link TicketCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Ticket> createSpecification(TicketCriteria criteria) {
        Specification<Ticket> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Ticket_.user, JoinType.LEFT);
                root.fetch(Ticket_.salon, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Ticket_.id),
                    buildRangeSpecification(criteria.getTicketNumber(), Ticket_.ticketNumber),
                    buildStringSpecification(criteria.getOwnerName(), Ticket_.ownerName),
                    buildSpecification(criteria.getOwnerType(), Ticket_.ownerType),
                    buildSpecification(criteria.getStatus(), Ticket_.status),
                    buildSpecification(criteria.getCategory(), Ticket_.category),
                    buildRangeSpecification(criteria.getPeopleAhead(), Ticket_.peopleAhead),
                    buildRangeSpecification(criteria.getEstimatedWaitMinutes(), Ticket_.estimatedWaitMinutes),
                    buildRangeSpecification(criteria.getItemCount(), Ticket_.itemCount),
                    buildRangeSpecification(criteria.getServedAt(), Ticket_.servedAt),
                    buildRangeSpecification(criteria.getCancelledAt(), Ticket_.cancelledAt),
                    buildRangeSpecification(criteria.getCreatedDate(), Ticket_.createdDate),
                    buildRangeSpecification(criteria.getLastModifiedDate(), Ticket_.lastModifiedDate),
                    buildSpecification(criteria.getUserId(), root -> root.join(Ticket_.user, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getSalonId(), root -> root.join(Ticket_.salon, JoinType.LEFT).get(Salon_.id))
                )
            );
        }
        return specification;
    }
}
