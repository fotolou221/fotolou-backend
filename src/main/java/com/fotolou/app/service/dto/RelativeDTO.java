package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.RelativeRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.Relative} entity.
 */
@Schema(description = "Proche enregistré par un client pour réservation multi-tickets")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RelativeDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    private RelativeRelation relation;

    @Size(max = 30)
    private String phone;

    private Instant createdDate;

    private UserDTO user;

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

    public RelativeRelation getRelation() {
        return relation;
    }

    public void setRelation(RelativeRelation relation) {
        this.relation = relation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RelativeDTO)) {
            return false;
        }

        RelativeDTO relativeDTO = (RelativeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, relativeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RelativeDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", relation='" + getRelation() + "'" +
            ", phone='" + getPhone() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
