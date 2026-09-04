package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppNotificationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppNotificationDTO.class);
        AppNotificationDTO appNotificationDTO1 = new AppNotificationDTO();
        appNotificationDTO1.setId(1L);
        AppNotificationDTO appNotificationDTO2 = new AppNotificationDTO();
        assertThat(appNotificationDTO1).isNotEqualTo(appNotificationDTO2);
        appNotificationDTO2.setId(appNotificationDTO1.getId());
        assertThat(appNotificationDTO1).isEqualTo(appNotificationDTO2);
        appNotificationDTO2.setId(2L);
        assertThat(appNotificationDTO1).isNotEqualTo(appNotificationDTO2);
        appNotificationDTO1.setId(null);
        assertThat(appNotificationDTO1).isNotEqualTo(appNotificationDTO2);
    }
}
