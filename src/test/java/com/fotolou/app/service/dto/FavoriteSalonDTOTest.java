package com.fotolou.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriteSalonDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FavoriteSalonDTO.class);
        FavoriteSalonDTO favoriteSalonDTO1 = new FavoriteSalonDTO();
        favoriteSalonDTO1.setId(1L);
        FavoriteSalonDTO favoriteSalonDTO2 = new FavoriteSalonDTO();
        assertThat(favoriteSalonDTO1).isNotEqualTo(favoriteSalonDTO2);
        favoriteSalonDTO2.setId(favoriteSalonDTO1.getId());
        assertThat(favoriteSalonDTO1).isEqualTo(favoriteSalonDTO2);
        favoriteSalonDTO2.setId(2L);
        assertThat(favoriteSalonDTO1).isNotEqualTo(favoriteSalonDTO2);
        favoriteSalonDTO1.setId(null);
        assertThat(favoriteSalonDTO1).isNotEqualTo(favoriteSalonDTO2);
    }
}
