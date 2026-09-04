package com.fotolou.app.domain;

import com.fotolou.app.domain.enumeration.OtpStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Historique des vérifications OTP par SMS
 */
@Entity
@Table(name = "otp_verification")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OtpVerification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 30)
    @Column(name = "phone", length = 30, nullable = false)
    private String phone;

    @NotNull
    @Size(max = 255)
    @Column(name = "code_hash", length = 255, nullable = false)
    private String codeHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OtpStatus status;

    @NotNull
    @Min(value = 0)
    @Column(name = "attempts_count", nullable = false)
    private Integer attemptsCount;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public OtpVerification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return this.phone;
    }

    public OtpVerification phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCodeHash() {
        return this.codeHash;
    }

    public OtpVerification codeHash(String codeHash) {
        this.setCodeHash(codeHash);
        return this;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public OtpStatus getStatus() {
        return this.status;
    }

    public OtpVerification status(OtpStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public Integer getAttemptsCount() {
        return this.attemptsCount;
    }

    public OtpVerification attemptsCount(Integer attemptsCount) {
        this.setAttemptsCount(attemptsCount);
        return this;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public OtpVerification expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public OtpVerification createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OtpVerification)) {
            return false;
        }
        return getId() != null && getId().equals(((OtpVerification) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OtpVerification{" +
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
