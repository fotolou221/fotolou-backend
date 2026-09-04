package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OtpVerificationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(OtpVerificationDTO.class);
        OtpVerificationDTO otpVerificationDTO1 = new OtpVerificationDTO();
        otpVerificationDTO1.setId(1L);
        OtpVerificationDTO otpVerificationDTO2 = new OtpVerificationDTO();
        assertThat(otpVerificationDTO1).isNotEqualTo(otpVerificationDTO2);
        otpVerificationDTO2.setId(otpVerificationDTO1.getId());
        assertThat(otpVerificationDTO1).isEqualTo(otpVerificationDTO2);
        otpVerificationDTO2.setId(2L);
        assertThat(otpVerificationDTO1).isNotEqualTo(otpVerificationDTO2);
        otpVerificationDTO1.setId(null);
        assertThat(otpVerificationDTO1).isNotEqualTo(otpVerificationDTO2);
    }
}
