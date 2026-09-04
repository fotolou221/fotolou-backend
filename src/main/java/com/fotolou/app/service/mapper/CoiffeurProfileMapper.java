package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.CoiffeurProfile;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.CoiffeurProfileDTO;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CoiffeurProfile} and its DTO {@link CoiffeurProfileDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoiffeurProfileMapper extends EntityMapper<CoiffeurProfileDTO, CoiffeurProfile> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    @Mapping(target = "salon", source = "salon", qualifiedByName = "salonName")
    CoiffeurProfileDTO toDto(CoiffeurProfile s);

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
