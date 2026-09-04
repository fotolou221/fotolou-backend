package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.FavoriteSalon;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.FavoriteSalonDTO;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FavoriteSalon} and its DTO {@link FavoriteSalonDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteSalonMapper extends EntityMapper<FavoriteSalonDTO, FavoriteSalon> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    @Mapping(target = "salon", source = "salon", qualifiedByName = "salonId")
    FavoriteSalonDTO toDto(FavoriteSalon s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("salonId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    SalonDTO toDtoSalonId(Salon salon);
}
