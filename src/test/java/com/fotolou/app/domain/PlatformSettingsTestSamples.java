package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PlatformSettingsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static PlatformSettings getPlatformSettingsSample1() {
        return new PlatformSettings()
            .id(1L)
            .appName("appName1")
            .contactEmail("contactEmail1")
            .contactPhone("contactPhone1")
            .openingTime("openingTime1")
            .closingTime("closingTime1");
    }

    public static PlatformSettings getPlatformSettingsSample2() {
        return new PlatformSettings()
            .id(2L)
            .appName("appName2")
            .contactEmail("contactEmail2")
            .contactPhone("contactPhone2")
            .openingTime("openingTime2")
            .closingTime("closingTime2");
    }

    public static PlatformSettings getPlatformSettingsRandomSampleGenerator() {
        return new PlatformSettings()
            .id(longCount.incrementAndGet())
            .appName(UUID.randomUUID().toString())
            .contactEmail(UUID.randomUUID().toString())
            .contactPhone(UUID.randomUUID().toString())
            .openingTime(UUID.randomUUID().toString())
            .closingTime(UUID.randomUUID().toString());
    }
}
