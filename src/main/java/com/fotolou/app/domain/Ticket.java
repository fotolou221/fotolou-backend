package com.fotolou.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Ticket virtuel de file d'attente
 */
@Entity
@Table(name = "ticket")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Ticket implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @NotNull
    @Size(max = 100)
    @Column(name = "owner_name", length = 100, nullable = false)
    private String ownerName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private TicketOwnerType ownerType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TicketCategory category;

    @Min(value = 0)
    @Column(name = "people_ahead")
    private Integer peopleAhead;

    @Min(value = 0)
    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @Min(value = 1)
    @Column(name = "item_count")
    private Integer itemCount;

    @Column(name = "served_at")
    private Instant servedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @NotNull
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "actionses", "coiffeurses", "ticketses" }, allowSetters = true)
    private Salon salon;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Ticket id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTicketNumber() {
        return this.ticketNumber;
    }

    public Ticket ticketNumber(Integer ticketNumber) {
        this.setTicketNumber(ticketNumber);
        return this;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public Ticket ownerName(String ownerName) {
        this.setOwnerName(ownerName);
        return this;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public TicketOwnerType getOwnerType() {
        return this.ownerType;
    }

    public Ticket ownerType(TicketOwnerType ownerType) {
        this.setOwnerType(ownerType);
        return this;
    }

    public void setOwnerType(TicketOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public TicketStatus getStatus() {
        return this.status;
    }

    public Ticket status(TicketStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketCategory getCategory() {
        return this.category;
    }

    public Ticket category(TicketCategory category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public Integer getPeopleAhead() {
        return this.peopleAhead;
    }

    public Ticket peopleAhead(Integer peopleAhead) {
        this.setPeopleAhead(peopleAhead);
        return this;
    }

    public void setPeopleAhead(Integer peopleAhead) {
        this.peopleAhead = peopleAhead;
    }

    public Integer getEstimatedWaitMinutes() {
        return this.estimatedWaitMinutes;
    }

    public Ticket estimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.setEstimatedWaitMinutes(estimatedWaitMinutes);
        return this;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getItemCount() {
        return this.itemCount;
    }

    public Ticket itemCount(Integer itemCount) {
        this.setItemCount(itemCount);
        return this;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Instant getServedAt() {
        return this.servedAt;
    }

    public Ticket servedAt(Instant servedAt) {
        this.setServedAt(servedAt);
        return this;
    }

    public void setServedAt(Instant servedAt) {
        this.servedAt = servedAt;
    }

    public Instant getCancelledAt() {
        return this.cancelledAt;
    }

    public Ticket cancelledAt(Instant cancelledAt) {
        this.setCancelledAt(cancelledAt);
        return this;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Ticket createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Ticket lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Ticket user(User user) {
        this.setUser(user);
        return this;
    }

    public Salon getSalon() {
        return this.salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public Ticket salon(Salon salon) {
        this.setSalon(salon);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ticket)) {
            return false;
        }
        return getId() != null && getId().equals(((Ticket) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Ticket{" +
            "id=" + getId() +
            ", ticketNumber=" + getTicketNumber() +
            ", ownerName='" + getOwnerName() + "'" +
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
            "}";
    }
}
