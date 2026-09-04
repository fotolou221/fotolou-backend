package com.fotolou.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class BoutiqueOrderCriteriaTest {

    @Test
    void newBoutiqueOrderCriteriaHasAllFiltersNullTest() {
        var boutiqueOrderCriteria = new BoutiqueOrderCriteria();
        assertThat(boutiqueOrderCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void boutiqueOrderCriteriaFluentMethodsCreatesFiltersTest() {
        var boutiqueOrderCriteria = new BoutiqueOrderCriteria();

        setAllFilters(boutiqueOrderCriteria);

        assertThat(boutiqueOrderCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void boutiqueOrderCriteriaCopyCreatesNullFilterTest() {
        var boutiqueOrderCriteria = new BoutiqueOrderCriteria();
        var copy = boutiqueOrderCriteria.copy();

        assertThat(boutiqueOrderCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(boutiqueOrderCriteria)
        );
    }

    @Test
    void boutiqueOrderCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var boutiqueOrderCriteria = new BoutiqueOrderCriteria();
        setAllFilters(boutiqueOrderCriteria);

        var copy = boutiqueOrderCriteria.copy();

        assertThat(boutiqueOrderCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(boutiqueOrderCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var boutiqueOrderCriteria = new BoutiqueOrderCriteria();

        assertThat(boutiqueOrderCriteria).hasToString("BoutiqueOrderCriteria{}");
    }

    private static void setAllFilters(BoutiqueOrderCriteria boutiqueOrderCriteria) {
        boutiqueOrderCriteria.id();
        boutiqueOrderCriteria.orderNumber();
        boutiqueOrderCriteria.subtotal();
        boutiqueOrderCriteria.deliveryFee();
        boutiqueOrderCriteria.totalPrice();
        boutiqueOrderCriteria.status();
        boutiqueOrderCriteria.orderType();
        boutiqueOrderCriteria.deliveryAddress();
        boutiqueOrderCriteria.deliveryDistrict();
        boutiqueOrderCriteria.customerName();
        boutiqueOrderCriteria.customerPhone();
        boutiqueOrderCriteria.createdDate();
        boutiqueOrderCriteria.lastModifiedDate();
        boutiqueOrderCriteria.itemsId();
        boutiqueOrderCriteria.userId();
        boutiqueOrderCriteria.distinct();
    }

    private static Condition<BoutiqueOrderCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getOrderNumber()) &&
                condition.apply(criteria.getSubtotal()) &&
                condition.apply(criteria.getDeliveryFee()) &&
                condition.apply(criteria.getTotalPrice()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getOrderType()) &&
                condition.apply(criteria.getDeliveryAddress()) &&
                condition.apply(criteria.getDeliveryDistrict()) &&
                condition.apply(criteria.getCustomerName()) &&
                condition.apply(criteria.getCustomerPhone()) &&
                condition.apply(criteria.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedDate()) &&
                condition.apply(criteria.getItemsId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<BoutiqueOrderCriteria> copyFiltersAre(
        BoutiqueOrderCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getOrderNumber(), copy.getOrderNumber()) &&
                condition.apply(criteria.getSubtotal(), copy.getSubtotal()) &&
                condition.apply(criteria.getDeliveryFee(), copy.getDeliveryFee()) &&
                condition.apply(criteria.getTotalPrice(), copy.getTotalPrice()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getOrderType(), copy.getOrderType()) &&
                condition.apply(criteria.getDeliveryAddress(), copy.getDeliveryAddress()) &&
                condition.apply(criteria.getDeliveryDistrict(), copy.getDeliveryDistrict()) &&
                condition.apply(criteria.getCustomerName(), copy.getCustomerName()) &&
                condition.apply(criteria.getCustomerPhone(), copy.getCustomerPhone()) &&
                condition.apply(criteria.getCreatedDate(), copy.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedDate(), copy.getLastModifiedDate()) &&
                condition.apply(criteria.getItemsId(), copy.getItemsId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
