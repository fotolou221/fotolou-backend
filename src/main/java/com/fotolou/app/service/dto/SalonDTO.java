package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.SalonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.Salon} entity.
 */
@Schema(description = "Salon de coiffure référencé sur Fotolou")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalonDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(min = 2, max = 120)
    private String slug;

    @NotNull
    @Size(max = 255)
    private String location;

    @NotNull
    @Size(max = 100)
    private String district;

    @Size(max = 255)
    private String address;

    @NotNull
    private SalonStatus status;

    @Size(max = 30)
    private String phone;

    @Size(max = 100)
    private String openingHours;

    @Min(value = 0)
    private Integer estimatedWaitMinutes;

    @Min(value = 0)
    private Integer peopleWaiting;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 500)
    private String coverUrl;

    private Double latitude;

    private Double longitude;

    @NotNull
    private Boolean active;

    private Instant createdDate;

    private Instant lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SalonStatus getStatus() {
        return status;
    }

    public void setStatus(SalonStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getPeopleWaiting() {
        return peopleWaiting;
    }

    public void setPeopleWaiting(Integer peopleWaiting) {
        this.peopleWaiting = peopleWaiting;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SalonDTO)) {
            return false;
        }

        SalonDTO salonDTO = (SalonDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, salonDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalonDTO{" +
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
