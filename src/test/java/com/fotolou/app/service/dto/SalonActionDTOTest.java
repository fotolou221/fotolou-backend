package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalonActionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalonActionDTO.class);
        SalonActionDTO salonActionDTO1 = new SalonActionDTO();
        salonActionDTO1.setId(1L);
        SalonActionDTO salonActionDTO2 = new SalonActionDTO();
        assertThat(salonActionDTO1).isNotEqualTo(salonActionDTO2);
        salonActionDTO2.setId(salonActionDTO1.getId());
        assertThat(salonActionDTO1).isEqualTo(salonActionDTO2);
        salonActionDTO2.setId(2L);
        assertThat(salonActionDTO1).isNotEqualTo(salonActionDTO2);
        salonActionDTO1.setId(null);
        assertThat(salonActionDTO1).isNotEqualTo(salonActionDTO2);
    }
}
