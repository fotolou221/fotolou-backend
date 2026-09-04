package com.fotolou.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProductCategoryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ProductCategory getProductCategorySample1() {
        return new ProductCategory().id(1L).name("name1").slug("slug1").description("description1").image("image1").icon("icon1");
    }

    public static ProductCategory getProductCategorySample2() {
        return new ProductCategory().id(2L).name("name2").slug("slug2").description("description2").image("image2").icon("icon2");
    }

    public static ProductCategory getProductCategoryRandomSampleGenerator() {
        return new ProductCategory()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .slug(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .image(UUID.randomUUID().toString())
            .icon(UUID.randomUUID().toString());
    }
}
