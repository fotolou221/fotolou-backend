package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CoiffeurProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static CoiffeurProfile getCoiffeurProfileSample1() {
        return new CoiffeurProfile()
            .id(1L)
            .name("name1")
            .phone("phone1")
            .specialty("specialty1")
            .avatarUrl("avatarUrl1")
            .ticketsServedCount(1);
    }

    public static CoiffeurProfile getCoiffeurProfileSample2() {
        return new CoiffeurProfile()
            .id(2L)
            .name("name2")
            .phone("phone2")
            .specialty("specialty2")
            .avatarUrl("avatarUrl2")
            .ticketsServedCount(2);
    }

    public static CoiffeurProfile getCoiffeurProfileRandomSampleGenerator() {
        return new CoiffeurProfile()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .specialty(UUID.randomUUID().toString())
            .avatarUrl(UUID.randomUUID().toString())
            .ticketsServedCount(intCount.incrementAndGet());
    }
}
