package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ticket} and its DTO {@link TicketDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper extends EntityMapper<TicketDTO, Ticket> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    @Mapping(target = "salon", source = "salon", qualifiedByName = "salonName")
    TicketDTO toDto(Ticket s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("salonName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    SalonDTO toDtoSalonName(Salon salon);
}
