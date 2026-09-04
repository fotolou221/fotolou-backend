package com.fotolou.app.domain;

import static com.fotolou.app.domain.BoutiqueOrderTestSamples.*;
import static com.fotolou.app.domain.OrderItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BoutiqueOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BoutiqueOrder.class);
        BoutiqueOrder boutiqueOrder1 = getBoutiqueOrderSample1();
        BoutiqueOrder boutiqueOrder2 = new BoutiqueOrder();
        assertThat(boutiqueOrder1).isNotEqualTo(boutiqueOrder2);

        boutiqueOrder2.setId(boutiqueOrder1.getId());
        assertThat(boutiqueOrder1).isEqualTo(boutiqueOrder2);

        boutiqueOrder2 = getBoutiqueOrderSample2();
        assertThat(boutiqueOrder1).isNotEqualTo(boutiqueOrder2);
    }

    @Test
    void itemsTest() {
        BoutiqueOrder boutiqueOrder = getBoutiqueOrderRandomSampleGenerator();
        OrderItem orderItemBack = getOrderItemRandomSampleGenerator();

        boutiqueOrder.addItems(orderItemBack);
        assertThat(boutiqueOrder.getItemses()).containsOnly(orderItemBack);
        assertThat(orderItemBack.getOrder()).isEqualTo(boutiqueOrder);

        boutiqueOrder.removeItems(orderItemBack);
        assertThat(boutiqueOrder.getItemses()).doesNotContain(orderItemBack);
        assertThat(orderItemBack.getOrder()).isNull();

        boutiqueOrder.itemses(new HashSet<>(Set.of(orderItemBack)));
        assertThat(boutiqueOrder.getItemses()).containsOnly(orderItemBack);
        assertThat(orderItemBack.getOrder()).isEqualTo(boutiqueOrder);

        boutiqueOrder.setItemses(new HashSet<>());
        assertThat(boutiqueOrder.getItemses()).doesNotContain(orderItemBack);
        assertThat(orderItemBack.getOrder()).isNull();
    }
}
