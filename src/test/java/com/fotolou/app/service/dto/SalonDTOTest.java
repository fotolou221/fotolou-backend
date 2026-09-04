package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalonDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalonDTO.class);
        SalonDTO salonDTO1 = new SalonDTO();
        salonDTO1.setId(1L);
        SalonDTO salonDTO2 = new SalonDTO();
        assertThat(salonDTO1).isNotEqualTo(salonDTO2);
        salonDTO2.setId(salonDTO1.getId());
        assertThat(salonDTO1).isEqualTo(salonDTO2);
        salonDTO2.setId(2L);
        assertThat(salonDTO1).isNotEqualTo(salonDTO2);
        salonDTO1.setId(null);
        assertThat(salonDTO1).isNotEqualTo(salonDTO2);
    }
}
