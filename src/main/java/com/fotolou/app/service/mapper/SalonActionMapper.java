package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.SalonAction;
import com.fotolou.app.service.dto.SalonActionDTO;
import com.fotolou.app.service.dto.SalonDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SalonAction} and its DTO {@link SalonActionDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalonActionMapper extends EntityMapper<SalonActionDTO, SalonAction> {
    @Mapping(target = "salon", source = "salon", qualifiedByName = "salonName")
    SalonActionDTO toDto(SalonAction s);

    @Named("salonName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    SalonDTO toDtoSalonName(Salon salon);
}
