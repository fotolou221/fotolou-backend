package com.fotolou.app.domain;

import static com.fotolou.app.domain.AppNotificationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppNotificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppNotification.class);
        AppNotification appNotification1 = getAppNotificationSample1();
        AppNotification appNotification2 = new AppNotification();
        assertThat(appNotification1).isNotEqualTo(appNotification2);

        appNotification2.setId(appNotification1.getId());
        assertThat(appNotification1).isEqualTo(appNotification2);

        appNotification2 = getAppNotificationSample2();
        assertThat(appNotification1).isNotEqualTo(appNotification2);
    }
}
