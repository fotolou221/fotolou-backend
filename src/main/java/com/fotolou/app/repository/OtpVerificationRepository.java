package com.fotolou.app.repository;

import com.fotolou.app.domain.OtpVerification;
import com.fotolou.app.domain.enumeration.OtpStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the OtpVerification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByPhoneAndStatusOrderByCreatedDateDesc(String phone, OtpStatus status);

    long countByPhoneAndCreatedDateAfter(String phone, Instant after);
}
