package com.fotolou.app.service.impl;

import com.fotolou.app.domain.Ticket;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.service.TicketService;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Ticket}.
 */
@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserService userService;

    public TicketServiceImpl(TicketRepository ticketRepository, TicketMapper ticketMapper, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userService = userService;
    }

    @Override
    public TicketDTO save(TicketDTO ticketDTO) {
        LOG.debug("Request to save Ticket : {}", ticketDTO);
        if (ticketDTO.getTicketNumber() == null) {
            ticketDTO.setTicketNumber((int) ticketRepository.count() + 1);
        }
        if (ticketDTO.getOwnerType() == null) {
            ticketDTO.setOwnerType(com.fotolou.app.domain.enumeration.TicketOwnerType.SELF);
        }
        if (ticketDTO.getStatus() == null) {
            ticketDTO.setStatus(com.fotolou.app.domain.enumeration.TicketStatus.WAITING);
        }
        if (ticketDTO.getCategory() == null) {
            ticketDTO.setCategory(com.fotolou.app.domain.enumeration.TicketCategory.ACTIVE);
        }
        if (ticketDTO.getCreatedDate() == null) {
            ticketDTO.setCreatedDate(java.time.Instant.now());
        }
        if (ticketDTO.getUser() == null) {
            com.fotolou.app.security.SecurityUtils.getCurrentUserLogin()
                .flatMap(userService::findOneByLogin)
                .ifPresent(u -> {
                    com.fotolou.app.service.dto.UserDTO userDTO = new com.fotolou.app.service.dto.UserDTO();
                    userDTO.setId(u.getId());
                    userDTO.setLogin(u.getLogin());
                    ticketDTO.setUser(userDTO);
                });
        }
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toDto(ticket);
    }

    @Override
    public TicketDTO update(TicketDTO ticketDTO) {
        LOG.debug("Request to update Ticket : {}", ticketDTO);
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toDto(ticket);
    }

    @Override
    public Optional<TicketDTO> partialUpdate(TicketDTO ticketDTO) {
        LOG.debug("Request to partially update Ticket : {}", ticketDTO);

        return ticketRepository
            .findById(ticketDTO.getId())
            .map(existingTicket -> {
                ticketMapper.partialUpdate(existingTicket, ticketDTO);
                return existingTicket;
            })
            .map(ticketRepository::save)
            .map(ticketMapper::toDto);
    }

    @Override
    public Page<TicketDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ticketRepository.findAllWithEagerRelationships(pageable).map(ticketMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TicketDTO> findOne(Long id) {
        LOG.debug("Request to get Ticket : {}", id);
        return ticketRepository.findOneWithEagerRelationships(id).map(ticketMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Ticket : {}", id);
        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return ticketRepository.existsById(id);
    }
}
