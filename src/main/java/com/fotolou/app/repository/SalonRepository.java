package com.fotolou.app.repository;

import com.fotolou.app.domain.Salon;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Salon entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SalonRepository extends JpaRepository<Salon, Long>, JpaSpecificationExecutor<Salon> {
    Optional<Salon> findOneBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select salon from Salon salon where salon.id = :id")
    Optional<Salon> findByIdForUpdate(@Param("id") Long id);

    boolean existsBySlug(String slug);
}
