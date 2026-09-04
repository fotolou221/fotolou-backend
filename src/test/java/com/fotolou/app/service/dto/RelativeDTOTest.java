package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RelativeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RelativeDTO.class);
        RelativeDTO relativeDTO1 = new RelativeDTO();
        relativeDTO1.setId(1L);
        RelativeDTO relativeDTO2 = new RelativeDTO();
        assertThat(relativeDTO1).isNotEqualTo(relativeDTO2);
        relativeDTO2.setId(relativeDTO1.getId());
        assertThat(relativeDTO1).isEqualTo(relativeDTO2);
        relativeDTO2.setId(2L);
        assertThat(relativeDTO1).isNotEqualTo(relativeDTO2);
        relativeDTO1.setId(null);
        assertThat(relativeDTO1).isNotEqualTo(relativeDTO2);
    }
}
