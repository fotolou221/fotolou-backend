package com.fotolou.app.repository;

import com.fotolou.app.domain.AppNotification;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AppNotification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long>, JpaSpecificationExecutor<AppNotification> {
    @Query("select appNotification from AppNotification appNotification where appNotification.user.login = ?#{authentication.name}")
    List<AppNotification> findByUserIsCurrentUser();
}
