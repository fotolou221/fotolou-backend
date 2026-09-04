package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CoiffeurProfileDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CoiffeurProfileDTO.class);
        CoiffeurProfileDTO coiffeurProfileDTO1 = new CoiffeurProfileDTO();
        coiffeurProfileDTO1.setId(1L);
        CoiffeurProfileDTO coiffeurProfileDTO2 = new CoiffeurProfileDTO();
        assertThat(coiffeurProfileDTO1).isNotEqualTo(coiffeurProfileDTO2);
        coiffeurProfileDTO2.setId(coiffeurProfileDTO1.getId());
        assertThat(coiffeurProfileDTO1).isEqualTo(coiffeurProfileDTO2);
        coiffeurProfileDTO2.setId(2L);
        assertThat(coiffeurProfileDTO1).isNotEqualTo(coiffeurProfileDTO2);
        coiffeurProfileDTO1.setId(null);
        assertThat(coiffeurProfileDTO1).isNotEqualTo(coiffeurProfileDTO2);
    }
}
