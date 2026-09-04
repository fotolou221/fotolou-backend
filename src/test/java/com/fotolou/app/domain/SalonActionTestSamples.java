package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SalonActionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static SalonAction getSalonActionSample1() {
        return new SalonAction().id(1L).label("label1").href("href1").sortOrder(1);
    }

    public static SalonAction getSalonActionSample2() {
        return new SalonAction().id(2L).label("label2").href("href2").sortOrder(2);
    }

    public static SalonAction getSalonActionRandomSampleGenerator() {
        return new SalonAction()
            .id(longCount.incrementAndGet())
            .label(UUID.randomUUID().toString())
            .href(UUID.randomUUID().toString())
            .sortOrder(intCount.incrementAndGet());
    }
}
