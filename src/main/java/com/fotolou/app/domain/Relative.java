package com.fotolou.app.domain;

import com.fotolou.app.domain.enumeration.RelativeRelation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Proche enregistré par un client pour réservation multi-tickets
 */
@Entity
@Table(name = "relative")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Relative implements Serializable {

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
    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = false)
    private RelativeRelation relation;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Relative id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Relative name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RelativeRelation getRelation() {
        return this.relation;
    }

    public Relative relation(RelativeRelation relation) {
        this.setRelation(relation);
        return this;
    }

    public void setRelation(RelativeRelation relation) {
        this.relation = relation;
    }

    public String getPhone() {
        return this.phone;
    }

    public Relative phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Relative createdDate(Instant createdDate) {
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

    public Relative user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relative)) {
            return false;
        }
        return getId() != null && getId().equals(((Relative) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Relative{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", relation='" + getRelation() + "'" +
            ", phone='" + getPhone() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
