package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AppNotificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static AppNotification getAppNotificationSample1() {
        return new AppNotification().id(1L).title("title1").targetRoute("targetRoute1");
    }

    public static AppNotification getAppNotificationSample2() {
        return new AppNotification().id(2L).title("title2").targetRoute("targetRoute2");
    }

    public static AppNotification getAppNotificationRandomSampleGenerator() {
        return new AppNotification()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .targetRoute(UUID.randomUUID().toString());
    }
}
