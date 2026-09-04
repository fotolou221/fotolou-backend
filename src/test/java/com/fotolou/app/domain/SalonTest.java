package com.fotolou.app.domain;

import static com.fotolou.app.domain.CoiffeurProfileTestSamples.*;
import static com.fotolou.app.domain.SalonActionTestSamples.*;
import static com.fotolou.app.domain.SalonTestSamples.*;
import static com.fotolou.app.domain.TicketTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SalonTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Salon.class);
        Salon salon1 = getSalonSample1();
        Salon salon2 = new Salon();
        assertThat(salon1).isNotEqualTo(salon2);

        salon2.setId(salon1.getId());
        assertThat(salon1).isEqualTo(salon2);

        salon2 = getSalonSample2();
        assertThat(salon1).isNotEqualTo(salon2);
    }

    @Test
    void actionsTest() {
        Salon salon = getSalonRandomSampleGenerator();
        SalonAction salonActionBack = getSalonActionRandomSampleGenerator();

        salon.addActions(salonActionBack);
        assertThat(salon.getActionses()).containsOnly(salonActionBack);
        assertThat(salonActionBack.getSalon()).isEqualTo(salon);

        salon.removeActions(salonActionBack);
        assertThat(salon.getActionses()).doesNotContain(salonActionBack);
        assertThat(salonActionBack.getSalon()).isNull();

        salon.actionses(new HashSet<>(Set.of(salonActionBack)));
        assertThat(salon.getActionses()).containsOnly(salonActionBack);
        assertThat(salonActionBack.getSalon()).isEqualTo(salon);

        salon.setActionses(new HashSet<>());
        assertThat(salon.getActionses()).doesNotContain(salonActionBack);
        assertThat(salonActionBack.getSalon()).isNull();
    }

    @Test
    void coiffeursTest() {
        Salon salon = getSalonRandomSampleGenerator();
        CoiffeurProfile coiffeurProfileBack = getCoiffeurProfileRandomSampleGenerator();

        salon.addCoiffeurs(coiffeurProfileBack);
        assertThat(salon.getCoiffeurses()).containsOnly(coiffeurProfileBack);
        assertThat(coiffeurProfileBack.getSalon()).isEqualTo(salon);

        salon.removeCoiffeurs(coiffeurProfileBack);
        assertThat(salon.getCoiffeurses()).doesNotContain(coiffeurProfileBack);
        assertThat(coiffeurProfileBack.getSalon()).isNull();

        salon.coiffeurses(new HashSet<>(Set.of(coiffeurProfileBack)));
        assertThat(salon.getCoiffeurses()).containsOnly(coiffeurProfileBack);
        assertThat(coiffeurProfileBack.getSalon()).isEqualTo(salon);

        salon.setCoiffeurses(new HashSet<>());
        assertThat(salon.getCoiffeurses()).doesNotContain(coiffeurProfileBack);
        assertThat(coiffeurProfileBack.getSalon()).isNull();
    }

    @Test
    void ticketsTest() {
        Salon salon = getSalonRandomSampleGenerator();
        Ticket ticketBack = getTicketRandomSampleGenerator();

        salon.addTickets(ticketBack);
        assertThat(salon.getTicketses()).containsOnly(ticketBack);
        assertThat(ticketBack.getSalon()).isEqualTo(salon);

        salon.removeTickets(ticketBack);
        assertThat(salon.getTicketses()).doesNotContain(ticketBack);
        assertThat(ticketBack.getSalon()).isNull();

        salon.ticketses(new HashSet<>(Set.of(ticketBack)));
        assertThat(salon.getTicketses()).containsOnly(ticketBack);
        assertThat(ticketBack.getSalon()).isEqualTo(salon);

        salon.setTicketses(new HashSet<>());
        assertThat(salon.getTicketses()).doesNotContain(ticketBack);
        assertThat(ticketBack.getSalon()).isNull();
    }
}
