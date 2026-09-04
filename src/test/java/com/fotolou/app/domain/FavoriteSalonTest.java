package com.fotolou.app.domain;

import static com.fotolou.app.domain.FavoriteSalonTestSamples.*;
import static com.fotolou.app.domain.SalonTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriteSalonTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FavoriteSalon.class);
        FavoriteSalon favoriteSalon1 = getFavoriteSalonSample1();
        FavoriteSalon favoriteSalon2 = new FavoriteSalon();
        assertThat(favoriteSalon1).isNotEqualTo(favoriteSalon2);

        favoriteSalon2.setId(favoriteSalon1.getId());
        assertThat(favoriteSalon1).isEqualTo(favoriteSalon2);

        favoriteSalon2 = getFavoriteSalonSample2();
        assertThat(favoriteSalon1).isNotEqualTo(favoriteSalon2);
    }

    @Test
    void salonTest() {
        FavoriteSalon favoriteSalon = getFavoriteSalonRandomSampleGenerator();
        Salon salonBack = getSalonRandomSampleGenerator();

        favoriteSalon.setSalon(salonBack);
        assertThat(favoriteSalon.getSalon()).isEqualTo(salonBack);

        favoriteSalon.salon(null);
        assertThat(favoriteSalon.getSalon()).isNull();
    }
}
