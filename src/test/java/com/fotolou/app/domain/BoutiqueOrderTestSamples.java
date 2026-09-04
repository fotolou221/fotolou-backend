package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BoutiqueOrderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static BoutiqueOrder getBoutiqueOrderSample1() {
        return new BoutiqueOrder()
            .id(1L)
            .orderNumber("orderNumber1")
            .subtotal(1L)
            .deliveryFee(1L)
            .totalPrice(1L)
            .deliveryAddress("deliveryAddress1")
            .deliveryDistrict("deliveryDistrict1")
            .customerName("customerName1")
            .customerPhone("customerPhone1");
    }

    public static BoutiqueOrder getBoutiqueOrderSample2() {
        return new BoutiqueOrder()
            .id(2L)
            .orderNumber("orderNumber2")
            .subtotal(2L)
            .deliveryFee(2L)
            .totalPrice(2L)
            .deliveryAddress("deliveryAddress2")
            .deliveryDistrict("deliveryDistrict2")
            .customerName("customerName2")
            .customerPhone("customerPhone2");
    }

    public static BoutiqueOrder getBoutiqueOrderRandomSampleGenerator() {
        return new BoutiqueOrder()
            .id(longCount.incrementAndGet())
            .orderNumber(UUID.randomUUID().toString())
            .subtotal(longCount.incrementAndGet())
            .deliveryFee(longCount.incrementAndGet())
            .totalPrice(longCount.incrementAndGet())
            .deliveryAddress(UUID.randomUUID().toString())
            .deliveryDistrict(UUID.randomUUID().toString())
            .customerName(UUID.randomUUID().toString())
            .customerPhone(UUID.randomUUID().toString());
    }
}
