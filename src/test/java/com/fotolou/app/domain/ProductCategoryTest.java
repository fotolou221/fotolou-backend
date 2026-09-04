package com.fotolou.app.domain;

import static com.fotolou.app.domain.ProductCategoryTestSamples.*;
import static com.fotolou.app.domain.ProductTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fotolou.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductCategoryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductCategory.class);
        ProductCategory productCategory1 = getProductCategorySample1();
        ProductCategory productCategory2 = new ProductCategory();
        assertThat(productCategory1).isNotEqualTo(productCategory2);

        productCategory2.setId(productCategory1.getId());
        assertThat(productCategory1).isEqualTo(productCategory2);

        productCategory2 = getProductCategorySample2();
        assertThat(productCategory1).isNotEqualTo(productCategory2);
    }

    @Test
    void productsTest() {
        ProductCategory productCategory = getProductCategoryRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        productCategory.addProducts(productBack);
        assertThat(productCategory.getProductses()).containsOnly(productBack);
        assertThat(productBack.getCategory()).isEqualTo(productCategory);

        productCategory.removeProducts(productBack);
        assertThat(productCategory.getProductses()).doesNotContain(productBack);
        assertThat(productBack.getCategory()).isNull();

        productCategory.productses(new HashSet<>(Set.of(productBack)));
        assertThat(productCategory.getProductses()).containsOnly(productBack);
        assertThat(productBack.getCategory()).isEqualTo(productCategory);

        productCategory.setProductses(new HashSet<>());
        assertThat(productCategory.getProductses()).doesNotContain(productBack);
        assertThat(productBack.getCategory()).isNull();
    }
}
