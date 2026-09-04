package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.service.dto.SalonDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Salon} and its DTO {@link SalonDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalonMapper extends EntityMapper<SalonDTO, Salon> {}
