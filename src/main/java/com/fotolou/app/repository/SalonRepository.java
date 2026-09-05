package com.fotolou.app.repository;

import com.fotolou.app.domain.Salon;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Salon entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SalonRepository extends JpaRepository<Salon, Long>, JpaSpecificationExecutor<Salon> {
    Optional<Salon> findOneBySlug(String slug);
    boolean existsBySlug(String slug);
}
