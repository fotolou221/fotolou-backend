package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProductTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Product getProductSample1() {
        return new Product().id(1L).brand("brand1").title("title1").price(1L).oldPrice(1L);
    }

    public static Product getProductSample2() {
        return new Product().id(2L).brand("brand2").title("title2").price(2L).oldPrice(2L);
    }

    public static Product getProductRandomSampleGenerator() {
        return new Product()
            .id(longCount.incrementAndGet())
            .brand(UUID.randomUUID().toString())
            .title(UUID.randomUUID().toString())
            .price(longCount.incrementAndGet())
            .oldPrice(longCount.incrementAndGet());
    }
}
