package com.fotolou.app.repository;

import com.fotolou.app.domain.CoiffeurProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CoiffeurProfile entity.
 */
@Repository
public interface CoiffeurProfileRepository extends JpaRepository<CoiffeurProfile, Long>, JpaSpecificationExecutor<CoiffeurProfile> {
    @Query("select coiffeurProfile from CoiffeurProfile coiffeurProfile where coiffeurProfile.user.login = ?#{authentication.name}")
    List<CoiffeurProfile> findByUserIsCurrentUser();

    default Optional<CoiffeurProfile> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CoiffeurProfile> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CoiffeurProfile> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select coiffeurProfile from CoiffeurProfile coiffeurProfile left join fetch coiffeurProfile.salon",
        countQuery = "select count(coiffeurProfile) from CoiffeurProfile coiffeurProfile"
    )
    Page<CoiffeurProfile> findAllWithToOneRelationships(Pageable pageable);

    @Query("select coiffeurProfile from CoiffeurProfile coiffeurProfile left join fetch coiffeurProfile.salon")
    List<CoiffeurProfile> findAllWithToOneRelationships();

    @Query(
        "select coiffeurProfile from CoiffeurProfile coiffeurProfile left join fetch coiffeurProfile.salon where coiffeurProfile.id =:id"
    )
    Optional<CoiffeurProfile> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<CoiffeurProfile> findByPhone(String phone);

    @Query("select cp from CoiffeurProfile cp left join fetch cp.salon where cp.user.login = :login")
    Optional<CoiffeurProfile> findOneWithSalonByUserLogin(@Param("login") String login);

    List<CoiffeurProfile> findBySalonId(Long salonId);
}
