package com.fotolou.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Profil Coiffeur / Professionnel rattaché à un salon
 */
@Entity
@Table(name = "coiffeur_profile")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CoiffeurProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @NotNull
    @Size(max = 30)
    @Column(name = "phone", length = 30, nullable = false)
    private String phone;

    @Size(max = 200)
    @Column(name = "specialty", length = 200)
    private String specialty;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Min(value = 0)
    @Column(name = "tickets_served_count")
    private Integer ticketsServedCount;

    @Column(name = "created_date")
    private Instant createdDate;

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

    public CoiffeurProfile id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CoiffeurProfile name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public CoiffeurProfile phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialty() {
        return this.specialty;
    }

    public CoiffeurProfile specialty(String specialty) {
        this.setSpecialty(specialty);
        return this;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Boolean getActive() {
        return this.active;
    }

    public CoiffeurProfile active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public CoiffeurProfile avatarUrl(String avatarUrl) {
        this.setAvatarUrl(avatarUrl);
        return this;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getTicketsServedCount() {
        return this.ticketsServedCount;
    }

    public CoiffeurProfile ticketsServedCount(Integer ticketsServedCount) {
        this.setTicketsServedCount(ticketsServedCount);
        return this;
    }

    public void setTicketsServedCount(Integer ticketsServedCount) {
        this.ticketsServedCount = ticketsServedCount;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public CoiffeurProfile createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CoiffeurProfile user(User user) {
        this.setUser(user);
        return this;
    }

    public Salon getSalon() {
        return this.salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public CoiffeurProfile salon(Salon salon) {
        this.setSalon(salon);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoiffeurProfile)) {
            return false;
        }
        return getId() != null && getId().equals(((CoiffeurProfile) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CoiffeurProfile{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", phone='" + getPhone() + "'" +
            ", specialty='" + getSpecialty() + "'" +
            ", active='" + getActive() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", ticketsServedCount=" + getTicketsServedCount() +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
