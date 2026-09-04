package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.SalonActionIcon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.SalonAction} entity.
 */
@Schema(description = "Actions rapides associées à un salon (site web, itinéraire, appel, partage)")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalonActionDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String label;

    @NotNull
    private SalonActionIcon icon;

    @Size(max = 500)
    private String href;

    private Integer sortOrder;

    @NotNull
    private SalonDTO salon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SalonActionIcon getIcon() {
        return icon;
    }

    public void setIcon(SalonActionIcon icon) {
        this.icon = icon;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
        if (!(o instanceof SalonActionDTO)) {
            return false;
        }

        SalonActionDTO salonActionDTO = (SalonActionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, salonActionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalonActionDTO{" +
            "id=" + getId() +
            ", label='" + getLabel() + "'" +
            ", icon='" + getIcon() + "'" +
            ", href='" + getHref() + "'" +
            ", sortOrder=" + getSortOrder() +
            ", salon=" + getSalon() +
            "}";
    }
}
