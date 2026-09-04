package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RelativeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Relative getRelativeSample1() {
        return new Relative().id(1L).name("name1").phone("phone1");
    }

    public static Relative getRelativeSample2() {
        return new Relative().id(2L).name("name2").phone("phone2");
    }

    public static Relative getRelativeRandomSampleGenerator() {
        return new Relative().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString()).phone(UUID.randomUUID().toString());
    }
}
