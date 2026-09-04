package com.fotolou.app.domain;

import static com.fotolou.app.domain.OtpVerificationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OtpVerificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OtpVerification.class);
        OtpVerification otpVerification1 = getOtpVerificationSample1();
        OtpVerification otpVerification2 = new OtpVerification();
        assertThat(otpVerification1).isNotEqualTo(otpVerification2);

        otpVerification2.setId(otpVerification1.getId());
        assertThat(otpVerification1).isEqualTo(otpVerification2);

        otpVerification2 = getOtpVerificationSample2();
        assertThat(otpVerification1).isNotEqualTo(otpVerification2);
    }
}
