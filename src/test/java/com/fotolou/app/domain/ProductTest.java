package com.fotolou.app.domain;

import static com.fotolou.app.domain.ProductCategoryTestSamples.*;
import static com.fotolou.app.domain.ProductImageTestSamples.*;
import static com.fotolou.app.domain.ProductTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Product.class);
        Product product1 = getProductSample1();
        Product product2 = new Product();
        assertThat(product1).isNotEqualTo(product2);

        product2.setId(product1.getId());
        assertThat(product1).isEqualTo(product2);

        product2 = getProductSample2();
        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void imagesTest() {
        Product product = getProductRandomSampleGenerator();
        ProductImage productImageBack = getProductImageRandomSampleGenerator();

        product.addImages(productImageBack);
        assertThat(product.getImageses()).containsOnly(productImageBack);
        assertThat(productImageBack.getProduct()).isEqualTo(product);

        product.removeImages(productImageBack);
        assertThat(product.getImageses()).doesNotContain(productImageBack);
        assertThat(productImageBack.getProduct()).isNull();

        product.imageses(new HashSet<>(Set.of(productImageBack)));
        assertThat(product.getImageses()).containsOnly(productImageBack);
        assertThat(productImageBack.getProduct()).isEqualTo(product);

        product.setImageses(new HashSet<>());
        assertThat(product.getImageses()).doesNotContain(productImageBack);
        assertThat(productImageBack.getProduct()).isNull();
    }

    @Test
    void categoryTest() {
        Product product = getProductRandomSampleGenerator();
        ProductCategory productCategoryBack = getProductCategoryRandomSampleGenerator();

        product.setCategory(productCategoryBack);
        assertThat(product.getCategory()).isEqualTo(productCategoryBack);

        product.category(null);
        assertThat(product.getCategory()).isNull();
    }
}
