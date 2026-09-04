package com.fotolou.app.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.FavoriteSalon} entity.
 */
@Schema(description = "Salons favoris sauvegardés par les clients")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FavoriteSalonDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant createdDate;

    @NotNull
    private UserDTO user;

    @NotNull
    private SalonDTO salon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(o instanceof FavoriteSalonDTO)) {
            return false;
        }

        FavoriteSalonDTO favoriteSalonDTO = (FavoriteSalonDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, favoriteSalonDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FavoriteSalonDTO{" +
            "id=" + getId() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", user=" + getUser() +
            ", salon=" + getSalon() +
            "}";
    }
}
