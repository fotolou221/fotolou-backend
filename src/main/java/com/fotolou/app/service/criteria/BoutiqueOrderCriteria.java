package com.fotolou.app.service.criteria;

import com.fotolou.app.domain.enumeration.OrderStatus;
import com.fotolou.app.domain.enumeration.OrderType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.fotolou.app.domain.BoutiqueOrder} entity. This class is used
 * in {@link com.fotolou.app.web.rest.BoutiqueOrderResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /boutique-orders?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BoutiqueOrderCriteria implements Serializable, Criteria {

    /**
     * Class for filtering OrderStatus
     */
    public static class OrderStatusFilter extends Filter<OrderStatus> {

        public OrderStatusFilter() {}

        public OrderStatusFilter(OrderStatusFilter filter) {
            super(filter);
        }

        @Override
        public OrderStatusFilter copy() {
            return new OrderStatusFilter(this);
        }
    }

    /**
     * Class for filtering OrderType
     */
    public static class OrderTypeFilter extends Filter<OrderType> {

        public OrderTypeFilter() {}

        public OrderTypeFilter(OrderTypeFilter filter) {
            super(filter);
        }

        @Override
        public OrderTypeFilter copy() {
            return new OrderTypeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter orderNumber;

    private LongFilter subtotal;

    private LongFilter deliveryFee;

    private LongFilter totalPrice;

    private OrderStatusFilter status;

    private OrderTypeFilter orderType;

    private StringFilter deliveryAddress;

    private StringFilter deliveryDistrict;

    private StringFilter customerName;

    private StringFilter customerPhone;

    private InstantFilter createdDate;

    private InstantFilter lastModifiedDate;

    private LongFilter itemsId;

    private LongFilter userId;

    private Boolean distinct;

    public BoutiqueOrderCriteria() {}

    public BoutiqueOrderCriteria(BoutiqueOrderCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.orderNumber = other.optionalOrderNumber().map(StringFilter::copy).orElse(null);
        this.subtotal = other.optionalSubtotal().map(LongFilter::copy).orElse(null);
        this.deliveryFee = other.optionalDeliveryFee().map(LongFilter::copy).orElse(null);
        this.totalPrice = other.optionalTotalPrice().map(LongFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(OrderStatusFilter::copy).orElse(null);
        this.orderType = other.optionalOrderType().map(OrderTypeFilter::copy).orElse(null);
        this.deliveryAddress = other.optionalDeliveryAddress().map(StringFilter::copy).orElse(null);
        this.deliveryDistrict = other.optionalDeliveryDistrict().map(StringFilter::copy).orElse(null);
        this.customerName = other.optionalCustomerName().map(StringFilter::copy).orElse(null);
        this.customerPhone = other.optionalCustomerPhone().map(StringFilter::copy).orElse(null);
        this.createdDate = other.optionalCreatedDate().map(InstantFilter::copy).orElse(null);
        this.lastModifiedDate = other.optionalLastModifiedDate().map(InstantFilter::copy).orElse(null);
        this.itemsId = other.optionalItemsId().map(LongFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public BoutiqueOrderCriteria copy() {
        return new BoutiqueOrderCriteria(this);
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

    public StringFilter getOrderNumber() {
        return orderNumber;
    }

    public Optional<StringFilter> optionalOrderNumber() {
        return Optional.ofNullable(orderNumber);
    }

    public StringFilter orderNumber() {
        if (orderNumber == null) {
            setOrderNumber(new StringFilter());
        }
        return orderNumber;
    }

    public void setOrderNumber(StringFilter orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LongFilter getSubtotal() {
        return subtotal;
    }

    public Optional<LongFilter> optionalSubtotal() {
        return Optional.ofNullable(subtotal);
    }

    public LongFilter subtotal() {
        if (subtotal == null) {
            setSubtotal(new LongFilter());
        }
        return subtotal;
    }

    public void setSubtotal(LongFilter subtotal) {
        this.subtotal = subtotal;
    }

    public LongFilter getDeliveryFee() {
        return deliveryFee;
    }

    public Optional<LongFilter> optionalDeliveryFee() {
        return Optional.ofNullable(deliveryFee);
    }

    public LongFilter deliveryFee() {
        if (deliveryFee == null) {
            setDeliveryFee(new LongFilter());
        }
        return deliveryFee;
    }

    public void setDeliveryFee(LongFilter deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public LongFilter getTotalPrice() {
        return totalPrice;
    }

    public Optional<LongFilter> optionalTotalPrice() {
        return Optional.ofNullable(totalPrice);
    }

    public LongFilter totalPrice() {
        if (totalPrice == null) {
            setTotalPrice(new LongFilter());
        }
        return totalPrice;
    }

    public void setTotalPrice(LongFilter totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatusFilter getStatus() {
        return status;
    }

    public Optional<OrderStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public OrderStatusFilter status() {
        if (status == null) {
            setStatus(new OrderStatusFilter());
        }
        return status;
    }

    public void setStatus(OrderStatusFilter status) {
        this.status = status;
    }

    public OrderTypeFilter getOrderType() {
        return orderType;
    }

    public Optional<OrderTypeFilter> optionalOrderType() {
        return Optional.ofNullable(orderType);
    }

    public OrderTypeFilter orderType() {
        if (orderType == null) {
            setOrderType(new OrderTypeFilter());
        }
        return orderType;
    }

    public void setOrderType(OrderTypeFilter orderType) {
        this.orderType = orderType;
    }

    public StringFilter getDeliveryAddress() {
        return deliveryAddress;
    }

    public Optional<StringFilter> optionalDeliveryAddress() {
        return Optional.ofNullable(deliveryAddress);
    }

    public StringFilter deliveryAddress() {
        if (deliveryAddress == null) {
            setDeliveryAddress(new StringFilter());
        }
        return deliveryAddress;
    }

    public void setDeliveryAddress(StringFilter deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public StringFilter getDeliveryDistrict() {
        return deliveryDistrict;
    }

    public Optional<StringFilter> optionalDeliveryDistrict() {
        return Optional.ofNullable(deliveryDistrict);
    }

    public StringFilter deliveryDistrict() {
        if (deliveryDistrict == null) {
            setDeliveryDistrict(new StringFilter());
        }
        return deliveryDistrict;
    }

    public void setDeliveryDistrict(StringFilter deliveryDistrict) {
        this.deliveryDistrict = deliveryDistrict;
    }

    public StringFilter getCustomerName() {
        return customerName;
    }

    public Optional<StringFilter> optionalCustomerName() {
        return Optional.ofNullable(customerName);
    }

    public StringFilter customerName() {
        if (customerName == null) {
            setCustomerName(new StringFilter());
        }
        return customerName;
    }

    public void setCustomerName(StringFilter customerName) {
        this.customerName = customerName;
    }

    public StringFilter getCustomerPhone() {
        return customerPhone;
    }

    public Optional<StringFilter> optionalCustomerPhone() {
        return Optional.ofNullable(customerPhone);
    }

    public StringFilter customerPhone() {
        if (customerPhone == null) {
            setCustomerPhone(new StringFilter());
        }
        return customerPhone;
    }

    public void setCustomerPhone(StringFilter customerPhone) {
        this.customerPhone = customerPhone;
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

    public LongFilter getItemsId() {
        return itemsId;
    }

    public Optional<LongFilter> optionalItemsId() {
        return Optional.ofNullable(itemsId);
    }

    public LongFilter itemsId() {
        if (itemsId == null) {
            setItemsId(new LongFilter());
        }
        return itemsId;
    }

    public void setItemsId(LongFilter itemsId) {
        this.itemsId = itemsId;
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
        final BoutiqueOrderCriteria that = (BoutiqueOrderCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(orderNumber, that.orderNumber) &&
            Objects.equals(subtotal, that.subtotal) &&
            Objects.equals(deliveryFee, that.deliveryFee) &&
            Objects.equals(totalPrice, that.totalPrice) &&
            Objects.equals(status, that.status) &&
            Objects.equals(orderType, that.orderType) &&
            Objects.equals(deliveryAddress, that.deliveryAddress) &&
            Objects.equals(deliveryDistrict, that.deliveryDistrict) &&
            Objects.equals(customerName, that.customerName) &&
            Objects.equals(customerPhone, that.customerPhone) &&
            Objects.equals(createdDate, that.createdDate) &&
            Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
            Objects.equals(itemsId, that.itemsId) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            orderNumber,
            subtotal,
            deliveryFee,
            totalPrice,
            status,
            orderType,
            deliveryAddress,
            deliveryDistrict,
            customerName,
            customerPhone,
            createdDate,
            lastModifiedDate,
            itemsId,
            userId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BoutiqueOrderCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalOrderNumber().map(f -> "orderNumber=" + f + ", ").orElse("") +
            optionalSubtotal().map(f -> "subtotal=" + f + ", ").orElse("") +
            optionalDeliveryFee().map(f -> "deliveryFee=" + f + ", ").orElse("") +
            optionalTotalPrice().map(f -> "totalPrice=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalOrderType().map(f -> "orderType=" + f + ", ").orElse("") +
            optionalDeliveryAddress().map(f -> "deliveryAddress=" + f + ", ").orElse("") +
            optionalDeliveryDistrict().map(f -> "deliveryDistrict=" + f + ", ").orElse("") +
            optionalCustomerName().map(f -> "customerName=" + f + ", ").orElse("") +
            optionalCustomerPhone().map(f -> "customerPhone=" + f + ", ").orElse("") +
            optionalCreatedDate().map(f -> "createdDate=" + f + ", ").orElse("") +
            optionalLastModifiedDate().map(f -> "lastModifiedDate=" + f + ", ").orElse("") +
            optionalItemsId().map(f -> "itemsId=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
