package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.OtpVerification;
import com.fotolou.app.service.dto.OtpVerificationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link OtpVerification} and its DTO {@link OtpVerificationDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OtpVerificationMapper extends EntityMapper<OtpVerificationDTO, OtpVerification> {}
