package com.fotolou.app.repository;

import com.fotolou.app.domain.Relative;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Relative entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RelativeRepository extends JpaRepository<Relative, Long> {
    @Query("select relative from Relative relative where relative.user.login = ?#{authentication.name}")
    List<Relative> findByUserIsCurrentUser();

    @Query("select relative from Relative relative where relative.user.login = :login order by relative.createdDate desc, relative.id desc")
    Page<Relative> findByUserLogin(@Param("login") String login, Pageable pageable);

    @Query("select relative from Relative relative where relative.user.login = :login order by relative.createdDate desc, relative.id desc")
    List<Relative> findByUserLogin(@Param("login") String login);

    @Query("select relative from Relative relative where relative.id = :id and relative.user.login = :login")
    Optional<Relative> findByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);

    boolean existsByIdAndUserLogin(Long id, String login);

    @Query("select r.user.id, count(r) from Relative r where r.user.id in :userIds group by r.user.id")
    List<Object[]> countRelativesByUserIds(@Param("userIds") java.util.Collection<Long> userIds);

    long countByUserId(Long userId);
}
