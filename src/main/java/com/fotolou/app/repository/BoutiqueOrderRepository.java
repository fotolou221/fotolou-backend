package com.fotolou.app.repository;

import com.fotolou.app.domain.BoutiqueOrder;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BoutiqueOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BoutiqueOrderRepository extends JpaRepository<BoutiqueOrder, Long>, JpaSpecificationExecutor<BoutiqueOrder> {
    @Query(
        "select boutiqueOrder from BoutiqueOrder boutiqueOrder where boutiqueOrder.user.login = ?#{authentication.name} order by boutiqueOrder.createdDate desc"
    )
    List<BoutiqueOrder> findByUserIsCurrentUser();

    List<BoutiqueOrder> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<BoutiqueOrder> findTop10ByOrderByCreatedDateDesc();

    java.util.Optional<BoutiqueOrder> findByOrderNumber(String orderNumber);
}
