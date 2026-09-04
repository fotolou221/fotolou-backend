package com.fotolou.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fotolou.app.domain.enumeration.SalonStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Salon de coiffure référencé sur Fotolou
 */
@Entity
@Table(name = "salon")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Salon implements Serializable {

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
    @Size(min = 2, max = 120)
    @Column(name = "slug", length = 120, nullable = false, unique = true)
    private String slug;

    @NotNull
    @Size(max = 255)
    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @NotNull
    @Size(max = 100)
    @Column(name = "district", length = 100, nullable = false)
    private String district;

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SalonStatus status;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 100)
    @Column(name = "opening_hours", length = 100)
    private String openingHours;

    @Min(value = 0)
    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @Min(value = 0)
    @Column(name = "people_waiting")
    private Integer peopleWaiting;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 500)
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "salon")
    @JsonIgnoreProperties(value = { "salon" }, allowSetters = true)
    private Set<SalonAction> actionses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "salon")
    @JsonIgnoreProperties(value = { "user", "salon" }, allowSetters = true)
    private Set<CoiffeurProfile> coiffeurses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "salon")
    @JsonIgnoreProperties(value = { "user", "salon" }, allowSetters = true)
    private Set<Ticket> ticketses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Salon id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Salon name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return this.slug;
    }

    public Salon slug(String slug) {
        this.setSlug(slug);
        return this;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLocation() {
        return this.location;
    }

    public Salon location(String location) {
        this.setLocation(location);
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDistrict() {
        return this.district;
    }

    public Salon district(String district) {
        this.setDistrict(district);
        return this;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return this.address;
    }

    public Salon address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SalonStatus getStatus() {
        return this.status;
    }

    public Salon status(SalonStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(SalonStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return this.phone;
    }

    public Salon phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpeningHours() {
        return this.openingHours;
    }

    public Salon openingHours(String openingHours) {
        this.setOpeningHours(openingHours);
        return this;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Integer getEstimatedWaitMinutes() {
        return this.estimatedWaitMinutes;
    }

    public Salon estimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.setEstimatedWaitMinutes(estimatedWaitMinutes);
        return this;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getPeopleWaiting() {
        return this.peopleWaiting;
    }

    public Salon peopleWaiting(Integer peopleWaiting) {
        this.setPeopleWaiting(peopleWaiting);
        return this;
    }

    public void setPeopleWaiting(Integer peopleWaiting) {
        this.peopleWaiting = peopleWaiting;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public Salon avatarUrl(String avatarUrl) {
        this.setAvatarUrl(avatarUrl);
        return this;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public Salon coverUrl(String coverUrl) {
        this.setCoverUrl(coverUrl);
        return this;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Salon latitude(Double latitude) {
        this.setLatitude(latitude);
        return this;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public Salon longitude(Double longitude) {
        this.setLongitude(longitude);
        return this;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Salon active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Salon createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Salon lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<SalonAction> getActionses() {
        return this.actionses;
    }

    public void setActionses(Set<SalonAction> salonActions) {
        if (this.actionses != null) {
            this.actionses.forEach(i -> i.setSalon(null));
        }
        if (salonActions != null) {
            salonActions.forEach(i -> i.setSalon(this));
        }
        this.actionses = salonActions;
    }

    public Salon actionses(Set<SalonAction> salonActions) {
        this.setActionses(salonActions);
        return this;
    }

    public Salon addActions(SalonAction salonAction) {
        this.actionses.add(salonAction);
        salonAction.setSalon(this);
        return this;
    }

    public Salon removeActions(SalonAction salonAction) {
        this.actionses.remove(salonAction);
        salonAction.setSalon(null);
        return this;
    }

    public Set<CoiffeurProfile> getCoiffeurses() {
        return this.coiffeurses;
    }

    public void setCoiffeurses(Set<CoiffeurProfile> coiffeurProfiles) {
        if (this.coiffeurses != null) {
            this.coiffeurses.forEach(i -> i.setSalon(null));
        }
        if (coiffeurProfiles != null) {
            coiffeurProfiles.forEach(i -> i.setSalon(this));
        }
        this.coiffeurses = coiffeurProfiles;
    }

    public Salon coiffeurses(Set<CoiffeurProfile> coiffeurProfiles) {
        this.setCoiffeurses(coiffeurProfiles);
        return this;
    }

    public Salon addCoiffeurs(CoiffeurProfile coiffeurProfile) {
        this.coiffeurses.add(coiffeurProfile);
        coiffeurProfile.setSalon(this);
        return this;
    }

    public Salon removeCoiffeurs(CoiffeurProfile coiffeurProfile) {
        this.coiffeurses.remove(coiffeurProfile);
        coiffeurProfile.setSalon(null);
        return this;
    }

    public Set<Ticket> getTicketses() {
        return this.ticketses;
    }

    public void setTicketses(Set<Ticket> tickets) {
        if (this.ticketses != null) {
            this.ticketses.forEach(i -> i.setSalon(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setSalon(this));
        }
        this.ticketses = tickets;
    }

    public Salon ticketses(Set<Ticket> tickets) {
        this.setTicketses(tickets);
        return this;
    }

    public Salon addTickets(Ticket ticket) {
        this.ticketses.add(ticket);
        ticket.setSalon(this);
        return this;
    }

    public Salon removeTickets(Ticket ticket) {
        this.ticketses.remove(ticket);
        ticket.setSalon(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Salon)) {
            return false;
        }
        return getId() != null && getId().equals(((Salon) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Salon{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", slug='" + getSlug() + "'" +
            ", location='" + getLocation() + "'" +
            ", district='" + getDistrict() + "'" +
            ", address='" + getAddress() + "'" +
            ", status='" + getStatus() + "'" +
            ", phone='" + getPhone() + "'" +
            ", openingHours='" + getOpeningHours() + "'" +
            ", estimatedWaitMinutes=" + getEstimatedWaitMinutes() +
            ", peopleWaiting=" + getPeopleWaiting() +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", coverUrl='" + getCoverUrl() + "'" +
            ", latitude=" + getLatitude() +
            ", longitude=" + getLongitude() +
            ", active='" + getActive() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
