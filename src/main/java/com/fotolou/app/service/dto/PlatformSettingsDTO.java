package com.fotolou.app.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.PlatformSettings} entity.
 */
@Schema(description = "Configuration globale de la plateforme gérée par les administrateurs")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PlatformSettingsDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String appName;

    @Size(max = 100)
    private String contactEmail;

    @Size(max = 30)
    private String contactPhone;

    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    private Double commissionRate;

    @Size(max = 10)
    private String openingTime;

    @Size(max = 10)
    private String closingTime;

    @NotNull
    private Boolean allowRelativeBooking;

    @NotNull
    private Boolean maintenanceMode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public Boolean getAllowRelativeBooking() {
        return allowRelativeBooking;
    }

    public void setAllowRelativeBooking(Boolean allowRelativeBooking) {
        this.allowRelativeBooking = allowRelativeBooking;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformSettingsDTO)) {
            return false;
        }

        PlatformSettingsDTO platformSettingsDTO = (PlatformSettingsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, platformSettingsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PlatformSettingsDTO{" +
            "id=" + getId() +
            ", appName='" + getAppName() + "'" +
            ", contactEmail='" + getContactEmail() + "'" +
            ", contactPhone='" + getContactPhone() + "'" +
            ", commissionRate=" + getCommissionRate() +
            ", openingTime='" + getOpeningTime() + "'" +
            ", closingTime='" + getClosingTime() + "'" +
            ", allowRelativeBooking='" + getAllowRelativeBooking() + "'" +
            ", maintenanceMode='" + getMaintenanceMode() + "'" +
            "}";
    }
}
