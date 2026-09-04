package com.fotolou.app.repository;

import com.fotolou.app.domain.SalonAction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SalonAction entity.
 */
@Repository
public interface SalonActionRepository extends JpaRepository<SalonAction, Long> {
    default Optional<SalonAction> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SalonAction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SalonAction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select salonAction from SalonAction salonAction left join fetch salonAction.salon",
        countQuery = "select count(salonAction) from SalonAction salonAction"
    )
    Page<SalonAction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select salonAction from SalonAction salonAction left join fetch salonAction.salon")
    List<SalonAction> findAllWithToOneRelationships();

    @Query("select salonAction from SalonAction salonAction left join fetch salonAction.salon where salonAction.id =:id")
    Optional<SalonAction> findOneWithToOneRelationships(@Param("id") Long id);
}
