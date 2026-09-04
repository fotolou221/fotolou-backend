package com.fotolou.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FavoriteSalonTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static FavoriteSalon getFavoriteSalonSample1() {
        return new FavoriteSalon().id(1L);
    }

    public static FavoriteSalon getFavoriteSalonSample2() {
        return new FavoriteSalon().id(2L);
    }

    public static FavoriteSalon getFavoriteSalonRandomSampleGenerator() {
        return new FavoriteSalon().id(longCount.incrementAndGet());
    }
}
