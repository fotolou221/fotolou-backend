package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Ticket getTicketSample1() {
        return new Ticket().id(1L).ticketNumber(1).ownerName("ownerName1").peopleAhead(1).estimatedWaitMinutes(1).itemCount(1);
    }

    public static Ticket getTicketSample2() {
        return new Ticket().id(2L).ticketNumber(2).ownerName("ownerName2").peopleAhead(2).estimatedWaitMinutes(2).itemCount(2);
    }

    public static Ticket getTicketRandomSampleGenerator() {
        return new Ticket()
            .id(longCount.incrementAndGet())
            .ticketNumber(intCount.incrementAndGet())
            .ownerName(UUID.randomUUID().toString())
            .peopleAhead(intCount.incrementAndGet())
            .estimatedWaitMinutes(intCount.incrementAndGet())
            .itemCount(intCount.incrementAndGet());
    }
}
