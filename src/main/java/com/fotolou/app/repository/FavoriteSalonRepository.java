package com.fotolou.app.repository;

import com.fotolou.app.domain.FavoriteSalon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the FavoriteSalon entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FavoriteSalonRepository extends JpaRepository<FavoriteSalon, Long> {
    @Query("select favoriteSalon from FavoriteSalon favoriteSalon where favoriteSalon.user.login = ?#{authentication.name}")
    List<FavoriteSalon> findByUserIsCurrentUser();

    Optional<FavoriteSalon> findByUserIdAndSalonId(Long userId, Long salonId);

    boolean existsByUserIdAndSalonId(Long userId, Long salonId);

    void deleteByUserIdAndSalonId(Long userId, Long salonId);

    @Query("select fs from FavoriteSalon fs left join fetch fs.salon where fs.user.id = :userId")
    List<FavoriteSalon> findWithSalonByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
