package com.fotolou.app.service;

import com.fotolou.app.service.dto.TicketDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.Ticket}.
 */
public interface TicketService {
    /**
     * Save a ticket.
     *
     * @param ticketDTO the entity to save.
     * @return the persisted entity.
     */
    TicketDTO save(TicketDTO ticketDTO);

    /**
     * Update a ticket.
     *
     * @param ticketDTO the entity to save.
     * @return the persisted entity.
     */
    TicketDTO update(TicketDTO ticketDTO);

    /**
     * Partially update a ticket.
     *
     * @param ticketDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TicketDTO> partialUpdate(TicketDTO ticketDTO);

    /**
     * Get all the tickets with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<TicketDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get one ticket by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TicketDTO> findOne(Long id);

    /**
     * Delete the ticket by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if a ticket exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
