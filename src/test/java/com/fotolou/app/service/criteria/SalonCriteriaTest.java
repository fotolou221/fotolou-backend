package com.fotolou.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SalonCriteriaTest {

    @Test
    void newSalonCriteriaHasAllFiltersNullTest() {
        var salonCriteria = new SalonCriteria();
        assertThat(salonCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void salonCriteriaFluentMethodsCreatesFiltersTest() {
        var salonCriteria = new SalonCriteria();

        setAllFilters(salonCriteria);

        assertThat(salonCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void salonCriteriaCopyCreatesNullFilterTest() {
        var salonCriteria = new SalonCriteria();
        var copy = salonCriteria.copy();

        assertThat(salonCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(salonCriteria)
        );
    }

    @Test
    void salonCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var salonCriteria = new SalonCriteria();
        setAllFilters(salonCriteria);

        var copy = salonCriteria.copy();

        assertThat(salonCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(salonCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var salonCriteria = new SalonCriteria();

        assertThat(salonCriteria).hasToString("SalonCriteria{}");
    }

    private static void setAllFilters(SalonCriteria salonCriteria) {
        salonCriteria.id();
        salonCriteria.name();
        salonCriteria.slug();
        salonCriteria.location();
        salonCriteria.district();
        salonCriteria.address();
        salonCriteria.status();
        salonCriteria.phone();
        salonCriteria.openingHours();
        salonCriteria.estimatedWaitMinutes();
        salonCriteria.peopleWaiting();
        salonCriteria.avatarUrl();
        salonCriteria.coverUrl();
        salonCriteria.latitude();
        salonCriteria.longitude();
        salonCriteria.active();
        salonCriteria.createdDate();
        salonCriteria.lastModifiedDate();
        salonCriteria.actionsId();
        salonCriteria.coiffeursId();
        salonCriteria.ticketsId();
        salonCriteria.distinct();
    }

    private static Condition<SalonCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getSlug()) &&
                condition.apply(criteria.getLocation()) &&
                condition.apply(criteria.getDistrict()) &&
                condition.apply(criteria.getAddress()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getPhone()) &&
                condition.apply(criteria.getOpeningHours()) &&
                condition.apply(criteria.getEstimatedWaitMinutes()) &&
                condition.apply(criteria.getPeopleWaiting()) &&
                condition.apply(criteria.getAvatarUrl()) &&
                condition.apply(criteria.getCoverUrl()) &&
                condition.apply(criteria.getLatitude()) &&
                condition.apply(criteria.getLongitude()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedDate()) &&
                condition.apply(criteria.getActionsId()) &&
                condition.apply(criteria.getCoiffeursId()) &&
                condition.apply(criteria.getTicketsId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SalonCriteria> copyFiltersAre(SalonCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getSlug(), copy.getSlug()) &&
                condition.apply(criteria.getLocation(), copy.getLocation()) &&
                condition.apply(criteria.getDistrict(), copy.getDistrict()) &&
                condition.apply(criteria.getAddress(), copy.getAddress()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getPhone(), copy.getPhone()) &&
                condition.apply(criteria.getOpeningHours(), copy.getOpeningHours()) &&
                condition.apply(criteria.getEstimatedWaitMinutes(), copy.getEstimatedWaitMinutes()) &&
                condition.apply(criteria.getPeopleWaiting(), copy.getPeopleWaiting()) &&
                condition.apply(criteria.getAvatarUrl(), copy.getAvatarUrl()) &&
                condition.apply(criteria.getCoverUrl(), copy.getCoverUrl()) &&
                condition.apply(criteria.getLatitude(), copy.getLatitude()) &&
                condition.apply(criteria.getLongitude(), copy.getLongitude()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getCreatedDate(), copy.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedDate(), copy.getLastModifiedDate()) &&
                condition.apply(criteria.getActionsId(), copy.getActionsId()) &&
                condition.apply(criteria.getCoiffeursId(), copy.getCoiffeursId()) &&
                condition.apply(criteria.getTicketsId(), copy.getTicketsId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
