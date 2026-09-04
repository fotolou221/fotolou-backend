package com.fotolou.app.service.criteria;

import com.fotolou.app.domain.enumeration.SalonStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.fotolou.app.domain.Salon} entity. This class is used
 * in {@link com.fotolou.app.web.rest.SalonResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /salons?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalonCriteria implements Serializable, Criteria {

    /**
     * Class for filtering SalonStatus
     */
    public static class SalonStatusFilter extends Filter<SalonStatus> {

        public SalonStatusFilter() {}

        public SalonStatusFilter(SalonStatusFilter filter) {
            super(filter);
        }

        @Override
        public SalonStatusFilter copy() {
            return new SalonStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter slug;

    private StringFilter location;

    private StringFilter district;

    private StringFilter address;

    private SalonStatusFilter status;

    private StringFilter phone;

    private StringFilter openingHours;

    private IntegerFilter estimatedWaitMinutes;

    private IntegerFilter peopleWaiting;

    private StringFilter avatarUrl;

    private StringFilter coverUrl;

    private DoubleFilter latitude;

    private DoubleFilter longitude;

    private BooleanFilter active;

    private InstantFilter createdDate;

    private InstantFilter lastModifiedDate;

    private LongFilter actionsId;

    private LongFilter coiffeursId;

    private LongFilter ticketsId;

    private Boolean distinct;

    public SalonCriteria() {}

    public SalonCriteria(SalonCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.slug = other.optionalSlug().map(StringFilter::copy).orElse(null);
        this.location = other.optionalLocation().map(StringFilter::copy).orElse(null);
        this.district = other.optionalDistrict().map(StringFilter::copy).orElse(null);
        this.address = other.optionalAddress().map(StringFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(SalonStatusFilter::copy).orElse(null);
        this.phone = other.optionalPhone().map(StringFilter::copy).orElse(null);
        this.openingHours = other.optionalOpeningHours().map(StringFilter::copy).orElse(null);
        this.estimatedWaitMinutes = other.optionalEstimatedWaitMinutes().map(IntegerFilter::copy).orElse(null);
        this.peopleWaiting = other.optionalPeopleWaiting().map(IntegerFilter::copy).orElse(null);
        this.avatarUrl = other.optionalAvatarUrl().map(StringFilter::copy).orElse(null);
        this.coverUrl = other.optionalCoverUrl().map(StringFilter::copy).orElse(null);
        this.latitude = other.optionalLatitude().map(DoubleFilter::copy).orElse(null);
        this.longitude = other.optionalLongitude().map(DoubleFilter::copy).orElse(null);
        this.active = other.optionalActive().map(BooleanFilter::copy).orElse(null);
        this.createdDate = other.optionalCreatedDate().map(InstantFilter::copy).orElse(null);
        this.lastModifiedDate = other.optionalLastModifiedDate().map(InstantFilter::copy).orElse(null);
        this.actionsId = other.optionalActionsId().map(LongFilter::copy).orElse(null);
        this.coiffeursId = other.optionalCoiffeursId().map(LongFilter::copy).orElse(null);
        this.ticketsId = other.optionalTicketsId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SalonCriteria copy() {
        return new SalonCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public Optional<StringFilter> optionalName() {
        return Optional.ofNullable(name);
    }

    public StringFilter name() {
        if (name == null) {
            setName(new StringFilter());
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getSlug() {
        return slug;
    }

    public Optional<StringFilter> optionalSlug() {
        return Optional.ofNullable(slug);
    }

    public StringFilter slug() {
        if (slug == null) {
            setSlug(new StringFilter());
        }
        return slug;
    }

    public void setSlug(StringFilter slug) {
        this.slug = slug;
    }

    public StringFilter getLocation() {
        return location;
    }

    public Optional<StringFilter> optionalLocation() {
        return Optional.ofNullable(location);
    }

    public StringFilter location() {
        if (location == null) {
            setLocation(new StringFilter());
        }
        return location;
    }

    public void setLocation(StringFilter location) {
        this.location = location;
    }

    public StringFilter getDistrict() {
        return district;
    }

    public Optional<StringFilter> optionalDistrict() {
        return Optional.ofNullable(district);
    }

    public StringFilter district() {
        if (district == null) {
            setDistrict(new StringFilter());
        }
        return district;
    }

    public void setDistrict(StringFilter district) {
        this.district = district;
    }

    public StringFilter getAddress() {
        return address;
    }

    public Optional<StringFilter> optionalAddress() {
        return Optional.ofNullable(address);
    }

    public StringFilter address() {
        if (address == null) {
            setAddress(new StringFilter());
        }
        return address;
    }

    public void setAddress(StringFilter address) {
        this.address = address;
    }

    public SalonStatusFilter getStatus() {
        return status;
    }

    public Optional<SalonStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public SalonStatusFilter status() {
        if (status == null) {
            setStatus(new SalonStatusFilter());
        }
        return status;
    }

    public void setStatus(SalonStatusFilter status) {
        this.status = status;
    }

    public StringFilter getPhone() {
        return phone;
    }

    public Optional<StringFilter> optionalPhone() {
        return Optional.ofNullable(phone);
    }

    public StringFilter phone() {
        if (phone == null) {
            setPhone(new StringFilter());
        }
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public StringFilter getOpeningHours() {
        return openingHours;
    }

    public Optional<StringFilter> optionalOpeningHours() {
        return Optional.ofNullable(openingHours);
    }

    public StringFilter openingHours() {
        if (openingHours == null) {
            setOpeningHours(new StringFilter());
        }
        return openingHours;
    }

    public void setOpeningHours(StringFilter openingHours) {
        this.openingHours = openingHours;
    }

    public IntegerFilter getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public Optional<IntegerFilter> optionalEstimatedWaitMinutes() {
        return Optional.ofNullable(estimatedWaitMinutes);
    }

    public IntegerFilter estimatedWaitMinutes() {
        if (estimatedWaitMinutes == null) {
            setEstimatedWaitMinutes(new IntegerFilter());
        }
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(IntegerFilter estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public IntegerFilter getPeopleWaiting() {
        return peopleWaiting;
    }

    public Optional<IntegerFilter> optionalPeopleWaiting() {
        return Optional.ofNullable(peopleWaiting);
    }

    public IntegerFilter peopleWaiting() {
        if (peopleWaiting == null) {
            setPeopleWaiting(new IntegerFilter());
        }
        return peopleWaiting;
    }

    public void setPeopleWaiting(IntegerFilter peopleWaiting) {
        this.peopleWaiting = peopleWaiting;
    }

    public StringFilter getAvatarUrl() {
        return avatarUrl;
    }

    public Optional<StringFilter> optionalAvatarUrl() {
        return Optional.ofNullable(avatarUrl);
    }

    public StringFilter avatarUrl() {
        if (avatarUrl == null) {
            setAvatarUrl(new StringFilter());
        }
        return avatarUrl;
    }

    public void setAvatarUrl(StringFilter avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public StringFilter getCoverUrl() {
        return coverUrl;
    }

    public Optional<StringFilter> optionalCoverUrl() {
        return Optional.ofNullable(coverUrl);
    }

    public StringFilter coverUrl() {
        if (coverUrl == null) {
            setCoverUrl(new StringFilter());
        }
        return coverUrl;
    }

    public void setCoverUrl(StringFilter coverUrl) {
        this.coverUrl = coverUrl;
    }

    public DoubleFilter getLatitude() {
        return latitude;
    }

    public Optional<DoubleFilter> optionalLatitude() {
        return Optional.ofNullable(latitude);
    }

    public DoubleFilter latitude() {
        if (latitude == null) {
            setLatitude(new DoubleFilter());
        }
        return latitude;
    }

    public void setLatitude(DoubleFilter latitude) {
        this.latitude = latitude;
    }

    public DoubleFilter getLongitude() {
        return longitude;
    }

    public Optional<DoubleFilter> optionalLongitude() {
        return Optional.ofNullable(longitude);
    }

    public DoubleFilter longitude() {
        if (longitude == null) {
            setLongitude(new DoubleFilter());
        }
        return longitude;
    }

    public void setLongitude(DoubleFilter longitude) {
        this.longitude = longitude;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public Optional<BooleanFilter> optionalActive() {
        return Optional.ofNullable(active);
    }

    public BooleanFilter active() {
        if (active == null) {
            setActive(new BooleanFilter());
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public InstantFilter getCreatedDate() {
        return createdDate;
    }

    public Optional<InstantFilter> optionalCreatedDate() {
        return Optional.ofNullable(createdDate);
    }

    public InstantFilter createdDate() {
        if (createdDate == null) {
            setCreatedDate(new InstantFilter());
        }
        return createdDate;
    }

    public void setCreatedDate(InstantFilter createdDate) {
        this.createdDate = createdDate;
    }

    public InstantFilter getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Optional<InstantFilter> optionalLastModifiedDate() {
        return Optional.ofNullable(lastModifiedDate);
    }

    public InstantFilter lastModifiedDate() {
        if (lastModifiedDate == null) {
            setLastModifiedDate(new InstantFilter());
        }
        return lastModifiedDate;
    }

    public void setLastModifiedDate(InstantFilter lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public LongFilter getActionsId() {
        return actionsId;
    }

    public Optional<LongFilter> optionalActionsId() {
        return Optional.ofNullable(actionsId);
    }

    public LongFilter actionsId() {
        if (actionsId == null) {
            setActionsId(new LongFilter());
        }
        return actionsId;
    }

    public void setActionsId(LongFilter actionsId) {
        this.actionsId = actionsId;
    }

    public LongFilter getCoiffeursId() {
        return coiffeursId;
    }

    public Optional<LongFilter> optionalCoiffeursId() {
        return Optional.ofNullable(coiffeursId);
    }

    public LongFilter coiffeursId() {
        if (coiffeursId == null) {
            setCoiffeursId(new LongFilter());
        }
        return coiffeursId;
    }

    public void setCoiffeursId(LongFilter coiffeursId) {
        this.coiffeursId = coiffeursId;
    }

    public LongFilter getTicketsId() {
        return ticketsId;
    }

    public Optional<LongFilter> optionalTicketsId() {
        return Optional.ofNullable(ticketsId);
    }

    public LongFilter ticketsId() {
        if (ticketsId == null) {
            setTicketsId(new LongFilter());
        }
        return ticketsId;
    }

    public void setTicketsId(LongFilter ticketsId) {
        this.ticketsId = ticketsId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SalonCriteria that = (SalonCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(slug, that.slug) &&
            Objects.equals(location, that.location) &&
            Objects.equals(district, that.district) &&
            Objects.equals(address, that.address) &&
            Objects.equals(status, that.status) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(openingHours, that.openingHours) &&
            Objects.equals(estimatedWaitMinutes, that.estimatedWaitMinutes) &&
            Objects.equals(peopleWaiting, that.peopleWaiting) &&
            Objects.equals(avatarUrl, that.avatarUrl) &&
            Objects.equals(coverUrl, that.coverUrl) &&
            Objects.equals(latitude, that.latitude) &&
            Objects.equals(longitude, that.longitude) &&
            Objects.equals(active, that.active) &&
            Objects.equals(createdDate, that.createdDate) &&
            Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
            Objects.equals(actionsId, that.actionsId) &&
            Objects.equals(coiffeursId, that.coiffeursId) &&
            Objects.equals(ticketsId, that.ticketsId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            name,
            slug,
            location,
            district,
            address,
            status,
            phone,
            openingHours,
            estimatedWaitMinutes,
            peopleWaiting,
            avatarUrl,
            coverUrl,
            latitude,
            longitude,
            active,
            createdDate,
            lastModifiedDate,
            actionsId,
            coiffeursId,
            ticketsId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalonCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalSlug().map(f -> "slug=" + f + ", ").orElse("") +
            optionalLocation().map(f -> "location=" + f + ", ").orElse("") +
            optionalDistrict().map(f -> "district=" + f + ", ").orElse("") +
            optionalAddress().map(f -> "address=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalPhone().map(f -> "phone=" + f + ", ").orElse("") +
            optionalOpeningHours().map(f -> "openingHours=" + f + ", ").orElse("") +
            optionalEstimatedWaitMinutes().map(f -> "estimatedWaitMinutes=" + f + ", ").orElse("") +
            optionalPeopleWaiting().map(f -> "peopleWaiting=" + f + ", ").orElse("") +
            optionalAvatarUrl().map(f -> "avatarUrl=" + f + ", ").orElse("") +
            optionalCoverUrl().map(f -> "coverUrl=" + f + ", ").orElse("") +
            optionalLatitude().map(f -> "latitude=" + f + ", ").orElse("") +
            optionalLongitude().map(f -> "longitude=" + f + ", ").orElse("") +
            optionalActive().map(f -> "active=" + f + ", ").orElse("") +
            optionalCreatedDate().map(f -> "createdDate=" + f + ", ").orElse("") +
            optionalLastModifiedDate().map(f -> "lastModifiedDate=" + f + ", ").orElse("") +
            optionalActionsId().map(f -> "actionsId=" + f + ", ").orElse("") +
            optionalCoiffeursId().map(f -> "coiffeursId=" + f + ", ").orElse("") +
            optionalTicketsId().map(f -> "ticketsId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
