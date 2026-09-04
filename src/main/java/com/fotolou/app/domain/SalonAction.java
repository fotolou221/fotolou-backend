package com.fotolou.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fotolou.app.domain.enumeration.SalonActionIcon;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * Actions rapides associées à un salon (site web, itinéraire, appel, partage)
 */
@Entity
@Table(name = "salon_action")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalonAction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "label", length = 50, nullable = false)
    private String label;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "icon", nullable = false)
    private SalonActionIcon icon;

    @Size(max = 500)
    @Column(name = "href", length = 500)
    private String href;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "actionses", "coiffeurses", "ticketses" }, allowSetters = true)
    private Salon salon;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SalonAction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public SalonAction label(String label) {
        this.setLabel(label);
        return this;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SalonActionIcon getIcon() {
        return this.icon;
    }

    public SalonAction icon(SalonActionIcon icon) {
        this.setIcon(icon);
        return this;
    }

    public void setIcon(SalonActionIcon icon) {
        this.icon = icon;
    }

    public String getHref() {
        return this.href;
    }

    public SalonAction href(String href) {
        this.setHref(href);
        return this;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public SalonAction sortOrder(Integer sortOrder) {
        this.setSortOrder(sortOrder);
        return this;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Salon getSalon() {
        return this.salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public SalonAction salon(Salon salon) {
        this.setSalon(salon);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SalonAction)) {
            return false;
        }
        return getId() != null && getId().equals(((SalonAction) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalonAction{" +
            "id=" + getId() +
            ", label='" + getLabel() + "'" +
            ", icon='" + getIcon() + "'" +
            ", href='" + getHref() + "'" +
            ", sortOrder=" + getSortOrder() +
            "}";
    }
}
