package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OtpVerificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static OtpVerification getOtpVerificationSample1() {
        return new OtpVerification().id(1L).phone("phone1").codeHash("codeHash1").attemptsCount(1);
    }

    public static OtpVerification getOtpVerificationSample2() {
        return new OtpVerification().id(2L).phone("phone2").codeHash("codeHash2").attemptsCount(2);
    }

    public static OtpVerification getOtpVerificationRandomSampleGenerator() {
        return new OtpVerification()
            .id(longCount.incrementAndGet())
            .phone(UUID.randomUUID().toString())
            .codeHash(UUID.randomUUID().toString())
            .attemptsCount(intCount.incrementAndGet());
    }
}
