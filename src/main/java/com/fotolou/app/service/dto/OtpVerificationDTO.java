package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.OtpStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.OtpVerification} entity.
 */
@Schema(description = "Historique des vérifications OTP par SMS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OtpVerificationDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 30)
    private String phone;

    @NotNull
    @Size(max = 255)
    private String codeHash;

    @NotNull
    private OtpStatus status;

    @NotNull
    @Min(value = 0)
    private Integer attemptsCount;

    @NotNull
    private Instant expiresAt;

    @NotNull
    private Instant createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OtpVerificationDTO)) {
            return false;
        }

        OtpVerificationDTO otpVerificationDTO = (OtpVerificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, otpVerificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OtpVerificationDTO{" +
            "id=" + getId() +
            ", phone='" + getPhone() + "'" +
            ", codeHash='" + getCodeHash() + "'" +
            ", status='" + getStatus() + "'" +
            ", attemptsCount=" + getAttemptsCount() +
            ", expiresAt='" + getExpiresAt() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
