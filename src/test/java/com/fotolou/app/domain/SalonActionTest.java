package com.fotolou.app.domain;

import static com.fotolou.app.domain.SalonActionTestSamples.*;
import static com.fotolou.app.domain.SalonTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalonActionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalonAction.class);
        SalonAction salonAction1 = getSalonActionSample1();
        SalonAction salonAction2 = new SalonAction();
        assertThat(salonAction1).isNotEqualTo(salonAction2);

        salonAction2.setId(salonAction1.getId());
        assertThat(salonAction1).isEqualTo(salonAction2);

        salonAction2 = getSalonActionSample2();
        assertThat(salonAction1).isNotEqualTo(salonAction2);
    }

    @Test
    void salonTest() {
        SalonAction salonAction = getSalonActionRandomSampleGenerator();
        Salon salonBack = getSalonRandomSampleGenerator();

        salonAction.setSalon(salonBack);
        assertThat(salonAction.getSalon()).isEqualTo(salonBack);

        salonAction.salon(null);
        assertThat(salonAction.getSalon()).isNull();
    }
}
