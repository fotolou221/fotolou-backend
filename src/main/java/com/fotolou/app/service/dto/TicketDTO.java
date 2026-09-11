package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.Ticket} entity.
 */
@Schema(description = "Ticket virtuel de file d'attente")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TicketDTO implements Serializable {

    private Long id;

    @Min(value = 1)
    private Integer ticketNumber;

    @Min(value = 1)
    private Integer currentTicketNumber;

    private Boolean currentTicketIsYesterday;

    @NotNull
    @Size(max = 100)
    private String ownerName;

    @Size(max = 30)
    private String ownerPhone;

    private TicketOwnerType ownerType = TicketOwnerType.SELF;

    private TicketStatus status = TicketStatus.WAITING;

    private TicketCategory category = TicketCategory.ACTIVE;

    @Min(value = 0)
    private Integer peopleAhead;

    @Min(value = 0)
    private Integer estimatedWaitMinutes;

    @Min(value = 1)
    private Integer itemCount = 1;

    private Instant servedAt;

    private Instant cancelledAt;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private UserDTO user;

    @NotNull
    private SalonDTO salon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Integer getCurrentTicketNumber() {
        return currentTicketNumber;
    }

    public void setCurrentTicketNumber(Integer currentTicketNumber) {
        this.currentTicketNumber = currentTicketNumber;
    }

    public Boolean getCurrentTicketIsYesterday() {
        return currentTicketIsYesterday;
    }

    public void setCurrentTicketIsYesterday(Boolean currentTicketIsYesterday) {
        this.currentTicketIsYesterday = currentTicketIsYesterday;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public TicketOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(TicketOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public Integer getPeopleAhead() {
        return peopleAhead;
    }

    public void setPeopleAhead(Integer peopleAhead) {
        this.peopleAhead = peopleAhead;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Instant getServedAt() {
        return servedAt;
    }

    public void setServedAt(Instant servedAt) {
        this.servedAt = servedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public SalonDTO getSalon() {
        return salon;
    }

    public void setSalon(SalonDTO salon) {
        this.salon = salon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketDTO)) {
            return false;
        }

        TicketDTO ticketDTO = (TicketDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, ticketDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TicketDTO{" +
            "id=" + getId() +
            ", ticketNumber=" + getTicketNumber() +
            ", currentTicketNumber=" + getCurrentTicketNumber() +
            ", ownerName='" + getOwnerName() + "'" +
            ", ownerPhone='" + getOwnerPhone() + "'" +
            ", ownerType='" + getOwnerType() + "'" +
            ", status='" + getStatus() + "'" +
            ", category='" + getCategory() + "'" +
            ", peopleAhead=" + getPeopleAhead() +
            ", estimatedWaitMinutes=" + getEstimatedWaitMinutes() +
            ", itemCount=" + getItemCount() +
            ", servedAt='" + getServedAt() + "'" +
            ", cancelledAt='" + getCancelledAt() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", user=" + getUser() +
            ", salon=" + getSalon() +
            "}";
    }
}
