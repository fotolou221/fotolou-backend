package com.fotolou.app.domain;

import static com.fotolou.app.domain.CoiffeurProfileTestSamples.*;
import static com.fotolou.app.domain.SalonTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CoiffeurProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CoiffeurProfile.class);
        CoiffeurProfile coiffeurProfile1 = getCoiffeurProfileSample1();
        CoiffeurProfile coiffeurProfile2 = new CoiffeurProfile();
        assertThat(coiffeurProfile1).isNotEqualTo(coiffeurProfile2);

        coiffeurProfile2.setId(coiffeurProfile1.getId());
        assertThat(coiffeurProfile1).isEqualTo(coiffeurProfile2);

        coiffeurProfile2 = getCoiffeurProfileSample2();
        assertThat(coiffeurProfile1).isNotEqualTo(coiffeurProfile2);
    }

    @Test
    void salonTest() {
        CoiffeurProfile coiffeurProfile = getCoiffeurProfileRandomSampleGenerator();
        Salon salonBack = getSalonRandomSampleGenerator();

        coiffeurProfile.setSalon(salonBack);
        assertThat(coiffeurProfile.getSalon()).isEqualTo(salonBack);

        coiffeurProfile.salon(null);
        assertThat(coiffeurProfile.getSalon()).isNull();
    }
}
