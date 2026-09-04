package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.Relative;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Relative} and its DTO {@link RelativeDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RelativeMapper extends EntityMapper<RelativeDTO, Relative> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    RelativeDTO toDto(Relative s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
