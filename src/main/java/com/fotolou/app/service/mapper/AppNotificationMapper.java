package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.AppNotificationDTO;
import com.fotolou.app.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AppNotification} and its DTO {@link AppNotificationDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppNotificationMapper extends EntityMapper<AppNotificationDTO, AppNotification> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    AppNotificationDTO toDto(AppNotification s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
