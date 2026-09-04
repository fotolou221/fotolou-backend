package com.fotolou.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration globale de la plateforme gérée par les administrateurs
 */
@Entity
@Table(name = "platform_settings")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PlatformSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "app_name", length = 100, nullable = false)
    private String appName;

    @Size(max = 100)
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Size(max = 30)
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "commission_rate")
    private Double commissionRate;

    @Size(max = 10)
    @Column(name = "opening_time", length = 10)
    private String openingTime;

    @Size(max = 10)
    @Column(name = "closing_time", length = 10)
    private String closingTime;

    @NotNull
    @Column(name = "allow_relative_booking", nullable = false)
    private Boolean allowRelativeBooking;

    @NotNull
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PlatformSettings id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return this.appName;
    }

    public PlatformSettings appName(String appName) {
        this.setAppName(appName);
        return this;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public PlatformSettings contactEmail(String contactEmail) {
        this.setContactEmail(contactEmail);
        return this;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return this.contactPhone;
    }

    public PlatformSettings contactPhone(String contactPhone) {
        this.setContactPhone(contactPhone);
        return this;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Double getCommissionRate() {
        return this.commissionRate;
    }

    public PlatformSettings commissionRate(Double commissionRate) {
        this.setCommissionRate(commissionRate);
        return this;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getOpeningTime() {
        return this.openingTime;
    }

    public PlatformSettings openingTime(String openingTime) {
        this.setOpeningTime(openingTime);
        return this;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return this.closingTime;
    }

    public PlatformSettings closingTime(String closingTime) {
        this.setClosingTime(closingTime);
        return this;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public Boolean getAllowRelativeBooking() {
        return this.allowRelativeBooking;
    }

    public PlatformSettings allowRelativeBooking(Boolean allowRelativeBooking) {
        this.setAllowRelativeBooking(allowRelativeBooking);
        return this;
    }

    public void setAllowRelativeBooking(Boolean allowRelativeBooking) {
        this.allowRelativeBooking = allowRelativeBooking;
    }

    public Boolean getMaintenanceMode() {
        return this.maintenanceMode;
    }

    public PlatformSettings maintenanceMode(Boolean maintenanceMode) {
        this.setMaintenanceMode(maintenanceMode);
        return this;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformSettings)) {
            return false;
        }
        return getId() != null && getId().equals(((PlatformSettings) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PlatformSettings{" +
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
