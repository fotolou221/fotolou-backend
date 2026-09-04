package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PlatformSettingsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PlatformSettingsDTO.class);
        PlatformSettingsDTO platformSettingsDTO1 = new PlatformSettingsDTO();
        platformSettingsDTO1.setId(1L);
        PlatformSettingsDTO platformSettingsDTO2 = new PlatformSettingsDTO();
        assertThat(platformSettingsDTO1).isNotEqualTo(platformSettingsDTO2);
        platformSettingsDTO2.setId(platformSettingsDTO1.getId());
        assertThat(platformSettingsDTO1).isEqualTo(platformSettingsDTO2);
        platformSettingsDTO2.setId(2L);
        assertThat(platformSettingsDTO1).isNotEqualTo(platformSettingsDTO2);
        platformSettingsDTO1.setId(null);
        assertThat(platformSettingsDTO1).isNotEqualTo(platformSettingsDTO2);
    }
}
