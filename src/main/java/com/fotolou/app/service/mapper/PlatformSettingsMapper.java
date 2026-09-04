package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.PlatformSettings;
import com.fotolou.app.service.dto.PlatformSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PlatformSettings} and its DTO {@link PlatformSettingsDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformSettingsMapper extends EntityMapper<PlatformSettingsDTO, PlatformSettings> {}
