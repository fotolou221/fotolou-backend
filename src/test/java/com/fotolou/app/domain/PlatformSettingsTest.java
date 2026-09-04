package com.fotolou.app.domain;

import static com.fotolou.app.domain.PlatformSettingsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PlatformSettingsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PlatformSettings.class);
        PlatformSettings platformSettings1 = getPlatformSettingsSample1();
        PlatformSettings platformSettings2 = new PlatformSettings();
        assertThat(platformSettings1).isNotEqualTo(platformSettings2);

        platformSettings2.setId(platformSettings1.getId());
        assertThat(platformSettings1).isEqualTo(platformSettings2);

        platformSettings2 = getPlatformSettingsSample2();
        assertThat(platformSettings1).isNotEqualTo(platformSettings2);
    }
}
