package com.fotolou.app.service.dto;

import com.fotolou.app.domain.enumeration.NotificationType;
import com.fotolou.app.domain.enumeration.RecipientRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fotolou.app.domain.AppNotification} entity.
 */
@Schema(description = "Notification in-app et push")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppNotificationDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 150)
    private String title;

    @Lob
    private String message;

    @NotNull
    private NotificationType type;

    @NotNull
    private RecipientRole recipientRole;

    @NotNull
    private Boolean isRead;

    @Size(max = 255)
    private String targetRoute;

    @NotNull
    private Instant createdDate;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public RecipientRole getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(RecipientRole recipientRole) {
        this.recipientRole = recipientRole;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getTargetRoute() {
        return targetRoute;
    }

    public void setTargetRoute(String targetRoute) {
        this.targetRoute = targetRoute;
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
        if (!(o instanceof AppNotificationDTO)) {
            return false;
        }

        AppNotificationDTO appNotificationDTO = (AppNotificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, appNotificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppNotificationDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", message='" + getMessage() + "'" +
            ", type='" + getType() + "'" +
            ", recipientRole='" + getRecipientRole() + "'" +
            ", isRead='" + getIsRead() + "'" +
            ", targetRoute='" + getTargetRoute() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
