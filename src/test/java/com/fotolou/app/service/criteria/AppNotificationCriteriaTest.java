package com.fotolou.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AppNotificationCriteriaTest {

    @Test
    void newAppNotificationCriteriaHasAllFiltersNullTest() {
        var appNotificationCriteria = new AppNotificationCriteria();
        assertThat(appNotificationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void appNotificationCriteriaFluentMethodsCreatesFiltersTest() {
        var appNotificationCriteria = new AppNotificationCriteria();

        setAllFilters(appNotificationCriteria);

        assertThat(appNotificationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void appNotificationCriteriaCopyCreatesNullFilterTest() {
        var appNotificationCriteria = new AppNotificationCriteria();
        var copy = appNotificationCriteria.copy();

        assertThat(appNotificationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(appNotificationCriteria)
        );
    }

    @Test
    void appNotificationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var appNotificationCriteria = new AppNotificationCriteria();
        setAllFilters(appNotificationCriteria);

        var copy = appNotificationCriteria.copy();

        assertThat(appNotificationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(appNotificationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var appNotificationCriteria = new AppNotificationCriteria();

        assertThat(appNotificationCriteria).hasToString("AppNotificationCriteria{}");
    }

    private static void setAllFilters(AppNotificationCriteria appNotificationCriteria) {
        appNotificationCriteria.id();
        appNotificationCriteria.title();
        appNotificationCriteria.type();
        appNotificationCriteria.recipientRole();
        appNotificationCriteria.isRead();
        appNotificationCriteria.targetRoute();
        appNotificationCriteria.createdDate();
        appNotificationCriteria.userId();
        appNotificationCriteria.distinct();
    }

    private static Condition<AppNotificationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getRecipientRole()) &&
                condition.apply(criteria.getIsRead()) &&
                condition.apply(criteria.getTargetRoute()) &&
                condition.apply(criteria.getCreatedDate()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AppNotificationCriteria> copyFiltersAre(
        AppNotificationCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getRecipientRole(), copy.getRecipientRole()) &&
                condition.apply(criteria.getIsRead(), copy.getIsRead()) &&
                condition.apply(criteria.getTargetRoute(), copy.getTargetRoute()) &&
                condition.apply(criteria.getCreatedDate(), copy.getCreatedDate()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
