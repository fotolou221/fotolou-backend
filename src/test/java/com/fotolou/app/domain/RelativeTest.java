package com.fotolou.app.domain;

import static com.fotolou.app.domain.RelativeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RelativeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Relative.class);
        Relative relative1 = getRelativeSample1();
        Relative relative2 = new Relative();
        assertThat(relative1).isNotEqualTo(relative2);

        relative2.setId(relative1.getId());
        assertThat(relative1).isEqualTo(relative2);

        relative2 = getRelativeSample2();
        assertThat(relative1).isNotEqualTo(relative2);
    }
}
