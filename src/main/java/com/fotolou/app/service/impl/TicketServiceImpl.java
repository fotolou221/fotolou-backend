package com.fotolou.app.service.impl;

import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.service.TicketService;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
    private final SalonRepository salonRepository;

    public TicketServiceImpl(
        TicketRepository ticketRepository,
        TicketMapper ticketMapper,
        UserService userService,
        SalonRepository salonRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userService = userService;
        this.salonRepository = salonRepository;
    }

    @Override
    public TicketDTO save(TicketDTO ticketDTO) {
        LOG.debug("Request to save Ticket : {}", ticketDTO);
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
            ticketDTO.setCreatedDate(Instant.now());
        }
        if (ticketDTO.getTicketNumber() == null) {
            ticketDTO.setTicketNumber(nextTicketNumberForTicketDay(ticketDTO));
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

    private int nextTicketNumberForTicketDay(TicketDTO ticketDTO) {
        Long salonId = ticketDTO.getSalon() != null ? ticketDTO.getSalon().getId() : null;
        if (salonId == null) {
            return (int) ticketRepository.count() + 1;
        }

        salonRepository.findByIdForUpdate(salonId).orElseThrow(() -> new IllegalArgumentException("Salon introuvable ID : " + salonId));

        ZoneId ticketDayZone = ZoneId.systemDefault();
        LocalDate ticketDay = LocalDate.ofInstant(ticketDTO.getCreatedDate(), ticketDayZone);
        Instant startOfDay = ticketDay.atStartOfDay(ticketDayZone).toInstant();
        Instant startOfNextDay = ticketDay.plusDays(1).atStartOfDay(ticketDayZone).toInstant();
        Integer maxTicketNumber = ticketRepository.findMaxTicketNumberForSalonAndDay(salonId, startOfDay, startOfNextDay);
        return Optional.ofNullable(maxTicketNumber).orElse(0) + 1;
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
        return ticketRepository.findOneWithEagerRelationships(id).map(this::toDtoWithQueueState);
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

    private TicketDTO toDtoWithQueueState(Ticket ticket) {
        TicketDTO dto = ticketMapper.toDto(ticket);
        if (ticket.getSalon() != null && ticket.getSalon().getId() != null) {
            CurrentTicketInfo info = findCurrentTicketInfo(ticket.getSalon().getId());
            if (info != null) {
                dto.setCurrentTicketNumber(info.number());
                dto.setCurrentTicketIsYesterday(info.isYesterday());
            }
        }
        applySelfOwnerName(dto, ticket);
        enrichOwnerPhone(dto, ticket);
        return dto;
    }

    private void enrichOwnerPhone(TicketDTO dto, Ticket ticket) {
        if (dto != null && (dto.getOwnerPhone() == null || dto.getOwnerPhone().isBlank()) && ticket != null && ticket.getUser() != null) {
            String login = ticket.getUser().getLogin();
            if (login != null && !login.contains("@")) {
                dto.setOwnerPhone(login);
            }
        }
    }

    private void applySelfOwnerName(TicketDTO dto, Ticket ticket) {
        if (dto == null || ticket == null || ticket.getOwnerType() != TicketOwnerType.SELF || ticket.getUser() == null) {
            return;
        }

        String userName = userDisplayName(ticket.getUser());
        if (!userName.isBlank()) {
            dto.setOwnerName(userName);
        }
    }

    private String userDisplayName(User user) {
        if (user == null) {
            return "";
        }

        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        return (firstName + (lastName.isBlank() ? "" : " " + lastName)).trim();
    }

    private record CurrentTicketInfo(Integer number, boolean isYesterday) {}

    private CurrentTicketInfo findCurrentTicketInfo(Long salonId) {
        Ticket current = ticketRepository
            .findBySalonIdAndStatusInOrderByCreatedDateAscIdAsc(
                salonId,
                List.of(com.fotolou.app.domain.enumeration.TicketStatus.YOUR_TURN, com.fotolou.app.domain.enumeration.TicketStatus.WAITING)
            )
            .stream()
            .findFirst()
            .orElse(null);

        if (current == null) {
            return null;
        }

        boolean isYesterday = false;
        if (current.getCreatedDate() != null) {
            java.time.LocalDate ticketDate = current.getCreatedDate().atZone(java.time.ZoneId.of("Africa/Dakar")).toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Africa/Dakar"));
            isYesterday = ticketDate.isBefore(today);
        }

        return new CurrentTicketInfo(current.getTicketNumber(), isYesterday);
    }
}
