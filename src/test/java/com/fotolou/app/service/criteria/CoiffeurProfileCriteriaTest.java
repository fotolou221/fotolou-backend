package com.fotolou.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CoiffeurProfileCriteriaTest {

    @Test
    void newCoiffeurProfileCriteriaHasAllFiltersNullTest() {
        var coiffeurProfileCriteria = new CoiffeurProfileCriteria();
        assertThat(coiffeurProfileCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void coiffeurProfileCriteriaFluentMethodsCreatesFiltersTest() {
        var coiffeurProfileCriteria = new CoiffeurProfileCriteria();

        setAllFilters(coiffeurProfileCriteria);

        assertThat(coiffeurProfileCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void coiffeurProfileCriteriaCopyCreatesNullFilterTest() {
        var coiffeurProfileCriteria = new CoiffeurProfileCriteria();
        var copy = coiffeurProfileCriteria.copy();

        assertThat(coiffeurProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(coiffeurProfileCriteria)
        );
    }

    @Test
    void coiffeurProfileCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var coiffeurProfileCriteria = new CoiffeurProfileCriteria();
        setAllFilters(coiffeurProfileCriteria);

        var copy = coiffeurProfileCriteria.copy();

        assertThat(coiffeurProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(coiffeurProfileCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var coiffeurProfileCriteria = new CoiffeurProfileCriteria();

        assertThat(coiffeurProfileCriteria).hasToString("CoiffeurProfileCriteria{}");
    }

    private static void setAllFilters(CoiffeurProfileCriteria coiffeurProfileCriteria) {
        coiffeurProfileCriteria.id();
        coiffeurProfileCriteria.name();
        coiffeurProfileCriteria.phone();
        coiffeurProfileCriteria.specialty();
        coiffeurProfileCriteria.active();
        coiffeurProfileCriteria.avatarUrl();
        coiffeurProfileCriteria.ticketsServedCount();
        coiffeurProfileCriteria.createdDate();
        coiffeurProfileCriteria.userId();
        coiffeurProfileCriteria.salonId();
        coiffeurProfileCriteria.distinct();
    }

    private static Condition<CoiffeurProfileCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getPhone()) &&
                condition.apply(criteria.getSpecialty()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getAvatarUrl()) &&
                condition.apply(criteria.getTicketsServedCount()) &&
                condition.apply(criteria.getCreatedDate()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getSalonId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CoiffeurProfileCriteria> copyFiltersAre(
        CoiffeurProfileCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getPhone(), copy.getPhone()) &&
                condition.apply(criteria.getSpecialty(), copy.getSpecialty()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getAvatarUrl(), copy.getAvatarUrl()) &&
                condition.apply(criteria.getTicketsServedCount(), copy.getTicketsServedCount()) &&
                condition.apply(criteria.getCreatedDate(), copy.getCreatedDate()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getSalonId(), copy.getSalonId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
