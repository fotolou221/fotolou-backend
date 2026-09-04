package com.fotolou.app.repository;

import com.fotolou.app.domain.PlatformSettings;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PlatformSettings entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {}
