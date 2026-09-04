package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SalonTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Salon getSalonSample1() {
        return new Salon()
            .id(1L)
            .name("name1")
            .slug("slug1")
            .location("location1")
            .district("district1")
            .address("address1")
            .phone("phone1")
            .openingHours("openingHours1")
            .estimatedWaitMinutes(1)
            .peopleWaiting(1)
            .avatarUrl("avatarUrl1")
            .coverUrl("coverUrl1");
    }

    public static Salon getSalonSample2() {
        return new Salon()
            .id(2L)
            .name("name2")
            .slug("slug2")
            .location("location2")
            .district("district2")
            .address("address2")
            .phone("phone2")
            .openingHours("openingHours2")
            .estimatedWaitMinutes(2)
            .peopleWaiting(2)
            .avatarUrl("avatarUrl2")
            .coverUrl("coverUrl2");
    }

    public static Salon getSalonRandomSampleGenerator() {
        return new Salon()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .slug(UUID.randomUUID().toString())
            .location(UUID.randomUUID().toString())
            .district(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .openingHours(UUID.randomUUID().toString())
            .estimatedWaitMinutes(intCount.incrementAndGet())
            .peopleWaiting(intCount.incrementAndGet())
            .avatarUrl(UUID.randomUUID().toString())
            .coverUrl(UUID.randomUUID().toString());
    }
}
