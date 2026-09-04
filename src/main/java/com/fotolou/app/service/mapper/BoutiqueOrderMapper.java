package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BoutiqueOrder} and its DTO {@link BoutiqueOrderDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoutiqueOrderMapper extends EntityMapper<BoutiqueOrderDTO, BoutiqueOrder> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    BoutiqueOrderDTO toDto(BoutiqueOrder s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
