package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OrderItemTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static OrderItem getOrderItemSample1() {
        return new OrderItem().id(1L).quantity(1).unitPrice(1L).productTitle("productTitle1");
    }

    public static OrderItem getOrderItemSample2() {
        return new OrderItem().id(2L).quantity(2).unitPrice(2L).productTitle("productTitle2");
    }

    public static OrderItem getOrderItemRandomSampleGenerator() {
        return new OrderItem()
            .id(longCount.incrementAndGet())
            .quantity(intCount.incrementAndGet())
            .unitPrice(longCount.incrementAndGet())
            .productTitle(UUID.randomUUID().toString());
    }
}
