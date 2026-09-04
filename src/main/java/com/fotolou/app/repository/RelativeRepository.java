package com.fotolou.app.repository;

import com.fotolou.app.domain.Relative;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Relative entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RelativeRepository extends JpaRepository<Relative, Long> {
    @Query("select relative from Relative relative where relative.user.login = ?#{authentication.name}")
    List<Relative> findByUserIsCurrentUser();
}
