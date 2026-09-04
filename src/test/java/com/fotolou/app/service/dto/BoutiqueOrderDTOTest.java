package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BoutiqueOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BoutiqueOrderDTO.class);
        BoutiqueOrderDTO boutiqueOrderDTO1 = new BoutiqueOrderDTO();
        boutiqueOrderDTO1.setId(1L);
        BoutiqueOrderDTO boutiqueOrderDTO2 = new BoutiqueOrderDTO();
        assertThat(boutiqueOrderDTO1).isNotEqualTo(boutiqueOrderDTO2);
        boutiqueOrderDTO2.setId(boutiqueOrderDTO1.getId());
        assertThat(boutiqueOrderDTO1).isEqualTo(boutiqueOrderDTO2);
        boutiqueOrderDTO2.setId(2L);
        assertThat(boutiqueOrderDTO1).isNotEqualTo(boutiqueOrderDTO2);
        boutiqueOrderDTO1.setId(null);
        assertThat(boutiqueOrderDTO1).isNotEqualTo(boutiqueOrderDTO2);
    }
}
