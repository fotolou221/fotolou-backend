package com.fotolou.app.service.criteria;

import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.fotolou.app.domain.Ticket} entity. This class is used
 * in {@link com.fotolou.app.web.rest.TicketResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /tickets?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TicketCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TicketOwnerType
     */
    public static class TicketOwnerTypeFilter extends Filter<TicketOwnerType> {

        public TicketOwnerTypeFilter() {}

        public TicketOwnerTypeFilter(TicketOwnerTypeFilter filter) {
            super(filter);
        }

        @Override
        public TicketOwnerTypeFilter copy() {
            return new TicketOwnerTypeFilter(this);
        }
    }

    /**
     * Class for filtering TicketStatus
     */
    public static class TicketStatusFilter extends Filter<TicketStatus> {

        public TicketStatusFilter() {}

        public TicketStatusFilter(TicketStatusFilter filter) {
            super(filter);
        }

        @Override
        public TicketStatusFilter copy() {
            return new TicketStatusFilter(this);
        }
    }

    /**
     * Class for filtering TicketCategory
     */
    public static class TicketCategoryFilter extends Filter<TicketCategory> {

        public TicketCategoryFilter() {}

        public TicketCategoryFilter(TicketCategoryFilter filter) {
            super(filter);
        }

        @Override
        public TicketCategoryFilter copy() {
            return new TicketCategoryFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IntegerFilter ticketNumber;

    private StringFilter ownerName;

    private TicketOwnerTypeFilter ownerType;

    private TicketStatusFilter status;

    private TicketCategoryFilter category;

    private IntegerFilter peopleAhead;

    private IntegerFilter estimatedWaitMinutes;

    private IntegerFilter itemCount;

    private InstantFilter servedAt;

    private InstantFilter cancelledAt;

    private InstantFilter createdDate;

    private InstantFilter lastModifiedDate;

    private LongFilter userId;

    private LongFilter salonId;

    private Boolean distinct;

    public TicketCriteria() {}

    public TicketCriteria(TicketCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.ticketNumber = other.optionalTicketNumber().map(IntegerFilter::copy).orElse(null);
        this.ownerName = other.optionalOwnerName().map(StringFilter::copy).orElse(null);
        this.ownerType = other.optionalOwnerType().map(TicketOwnerTypeFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(TicketStatusFilter::copy).orElse(null);
        this.category = other.optionalCategory().map(TicketCategoryFilter::copy).orElse(null);
        this.peopleAhead = other.optionalPeopleAhead().map(IntegerFilter::copy).orElse(null);
        this.estimatedWaitMinutes = other.optionalEstimatedWaitMinutes().map(IntegerFilter::copy).orElse(null);
        this.itemCount = other.optionalItemCount().map(IntegerFilter::copy).orElse(null);
        this.servedAt = other.optionalServedAt().map(InstantFilter::copy).orElse(null);
        this.cancelledAt = other.optionalCancelledAt().map(InstantFilter::copy).orElse(null);
        this.createdDate = other.optionalCreatedDate().map(InstantFilter::copy).orElse(null);
        this.lastModifiedDate = other.optionalLastModifiedDate().map(InstantFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.salonId = other.optionalSalonId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TicketCriteria copy() {
        return new TicketCriteria(this);
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

    public IntegerFilter getTicketNumber() {
        return ticketNumber;
    }

    public Optional<IntegerFilter> optionalTicketNumber() {
        return Optional.ofNullable(ticketNumber);
    }

    public IntegerFilter ticketNumber() {
        if (ticketNumber == null) {
            setTicketNumber(new IntegerFilter());
        }
        return ticketNumber;
    }

    public void setTicketNumber(IntegerFilter ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public StringFilter getOwnerName() {
        return ownerName;
    }

    public Optional<StringFilter> optionalOwnerName() {
        return Optional.ofNullable(ownerName);
    }

    public StringFilter ownerName() {
        if (ownerName == null) {
            setOwnerName(new StringFilter());
        }
        return ownerName;
    }

    public void setOwnerName(StringFilter ownerName) {
        this.ownerName = ownerName;
    }

    public TicketOwnerTypeFilter getOwnerType() {
        return ownerType;
    }

    public Optional<TicketOwnerTypeFilter> optionalOwnerType() {
        return Optional.ofNullable(ownerType);
    }

    public TicketOwnerTypeFilter ownerType() {
        if (ownerType == null) {
            setOwnerType(new TicketOwnerTypeFilter());
        }
        return ownerType;
    }

    public void setOwnerType(TicketOwnerTypeFilter ownerType) {
        this.ownerType = ownerType;
    }

    public TicketStatusFilter getStatus() {
        return status;
    }

    public Optional<TicketStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public TicketStatusFilter status() {
        if (status == null) {
            setStatus(new TicketStatusFilter());
        }
        return status;
    }

    public void setStatus(TicketStatusFilter status) {
        this.status = status;
    }

    public TicketCategoryFilter getCategory() {
        return category;
    }

    public Optional<TicketCategoryFilter> optionalCategory() {
        return Optional.ofNullable(category);
    }

    public TicketCategoryFilter category() {
        if (category == null) {
            setCategory(new TicketCategoryFilter());
        }
        return category;
    }

    public void setCategory(TicketCategoryFilter category) {
        this.category = category;
    }

    public IntegerFilter getPeopleAhead() {
        return peopleAhead;
    }

    public Optional<IntegerFilter> optionalPeopleAhead() {
        return Optional.ofNullable(peopleAhead);
    }

    public IntegerFilter peopleAhead() {
        if (peopleAhead == null) {
            setPeopleAhead(new IntegerFilter());
        }
        return peopleAhead;
    }

    public void setPeopleAhead(IntegerFilter peopleAhead) {
        this.peopleAhead = peopleAhead;
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

    public IntegerFilter getItemCount() {
        return itemCount;
    }

    public Optional<IntegerFilter> optionalItemCount() {
        return Optional.ofNullable(itemCount);
    }

    public IntegerFilter itemCount() {
        if (itemCount == null) {
            setItemCount(new IntegerFilter());
        }
        return itemCount;
    }

    public void setItemCount(IntegerFilter itemCount) {
        this.itemCount = itemCount;
    }

    public InstantFilter getServedAt() {
        return servedAt;
    }

    public Optional<InstantFilter> optionalServedAt() {
        return Optional.ofNullable(servedAt);
    }

    public InstantFilter servedAt() {
        if (servedAt == null) {
            setServedAt(new InstantFilter());
        }
        return servedAt;
    }

    public void setServedAt(InstantFilter servedAt) {
        this.servedAt = servedAt;
    }

    public InstantFilter getCancelledAt() {
        return cancelledAt;
    }

    public Optional<InstantFilter> optionalCancelledAt() {
        return Optional.ofNullable(cancelledAt);
    }

    public InstantFilter cancelledAt() {
        if (cancelledAt == null) {
            setCancelledAt(new InstantFilter());
        }
        return cancelledAt;
    }

    public void setCancelledAt(InstantFilter cancelledAt) {
        this.cancelledAt = cancelledAt;
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

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public LongFilter getSalonId() {
        return salonId;
    }

    public Optional<LongFilter> optionalSalonId() {
        return Optional.ofNullable(salonId);
    }

    public LongFilter salonId() {
        if (salonId == null) {
            setSalonId(new LongFilter());
        }
        return salonId;
    }

    public void setSalonId(LongFilter salonId) {
        this.salonId = salonId;
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
        final TicketCriteria that = (TicketCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ticketNumber, that.ticketNumber) &&
            Objects.equals(ownerName, that.ownerName) &&
            Objects.equals(ownerType, that.ownerType) &&
            Objects.equals(status, that.status) &&
            Objects.equals(category, that.category) &&
            Objects.equals(peopleAhead, that.peopleAhead) &&
            Objects.equals(estimatedWaitMinutes, that.estimatedWaitMinutes) &&
            Objects.equals(itemCount, that.itemCount) &&
            Objects.equals(servedAt, that.servedAt) &&
            Objects.equals(cancelledAt, that.cancelledAt) &&
            Objects.equals(createdDate, that.createdDate) &&
            Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(salonId, that.salonId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            ticketNumber,
            ownerName,
            ownerType,
            status,
            category,
            peopleAhead,
            estimatedWaitMinutes,
            itemCount,
            servedAt,
            cancelledAt,
            createdDate,
            lastModifiedDate,
            userId,
            salonId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TicketCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTicketNumber().map(f -> "ticketNumber=" + f + ", ").orElse("") +
            optionalOwnerName().map(f -> "ownerName=" + f + ", ").orElse("") +
            optionalOwnerType().map(f -> "ownerType=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalCategory().map(f -> "category=" + f + ", ").orElse("") +
            optionalPeopleAhead().map(f -> "peopleAhead=" + f + ", ").orElse("") +
            optionalEstimatedWaitMinutes().map(f -> "estimatedWaitMinutes=" + f + ", ").orElse("") +
            optionalItemCount().map(f -> "itemCount=" + f + ", ").orElse("") +
            optionalServedAt().map(f -> "servedAt=" + f + ", ").orElse("") +
            optionalCancelledAt().map(f -> "cancelledAt=" + f + ", ").orElse("") +
            optionalCreatedDate().map(f -> "createdDate=" + f + ", ").orElse("") +
            optionalLastModifiedDate().map(f -> "lastModifiedDate=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalSalonId().map(f -> "salonId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
