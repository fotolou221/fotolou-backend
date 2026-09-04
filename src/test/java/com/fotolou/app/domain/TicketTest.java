package com.fotolou.app.domain;

import static com.fotolou.app.domain.SalonTestSamples.*;
import static com.fotolou.app.domain.TicketTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Ticket.class);
        Ticket ticket1 = getTicketSample1();
        Ticket ticket2 = new Ticket();
        assertThat(ticket1).isNotEqualTo(ticket2);

        ticket2.setId(ticket1.getId());
        assertThat(ticket1).isEqualTo(ticket2);

        ticket2 = getTicketSample2();
        assertThat(ticket1).isNotEqualTo(ticket2);
    }

    @Test
    void salonTest() {
        Ticket ticket = getTicketRandomSampleGenerator();
        Salon salonBack = getSalonRandomSampleGenerator();

        ticket.setSalon(salonBack);
        assertThat(ticket.getSalon()).isEqualTo(salonBack);

        ticket.salon(null);
        assertThat(ticket.getSalon()).isNull();
    }
}
