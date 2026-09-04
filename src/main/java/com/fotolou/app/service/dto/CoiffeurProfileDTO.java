package com.fotolou.app.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.CoiffeurProfile} entity.
 */
@Schema(description = "Profil Coiffeur / Professionnel rattaché à un salon")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CoiffeurProfileDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(max = 30)
    private String phone;

    @Size(max = 200)
    private String specialty;

    @NotNull
    private Boolean active;

    @Size(max = 500)
    private String avatarUrl;

    @Min(value = 0)
    private Integer ticketsServedCount;

    private Instant createdDate;

    private UserDTO user;

    @NotNull
    private SalonDTO salon;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getTicketsServedCount() {
        return ticketsServedCount;
    }

    public void setTicketsServedCount(Integer ticketsServedCount) {
        this.ticketsServedCount = ticketsServedCount;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
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
        if (!(o instanceof CoiffeurProfileDTO)) {
            return false;
        }

        CoiffeurProfileDTO coiffeurProfileDTO = (CoiffeurProfileDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, coiffeurProfileDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CoiffeurProfileDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", phone='" + getPhone() + "'" +
            ", specialty='" + getSpecialty() + "'" +
            ", active='" + getActive() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", ticketsServedCount=" + getTicketsServedCount() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", user=" + getUser() +
            ", salon=" + getSalon() +
            "}";
    }
}
