package springboot_login_page.login_page.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_logs_seq")
    @SequenceGenerator(name = "audit_logs_seq", sequenceName = "audit_logs_seq", allocationSize = 1)
    private Long id;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Column(name = "userid", nullable = false, length = 100)
    private String userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "typename", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entityid", length = 100)
    private String entityId;

    @Lob
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Column(name = "ipaddress", length = 50)
    private String ipAddress;

    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Pre-persist validation to ensure all NOT NULL fields have values
     * This provides an additional layer of protection at the entity level
     */
    @PrePersist
    public void prePersist() {
        // Set timestamp if not set (though CreationTimestamp should handle this)
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        // Ensure userId is not null
        if (userId == null || userId.trim().isEmpty()) {
            if (username != null && !username.trim().isEmpty()) {
                userId = username;
            } else {
                userId = "SYSTEM";
            }
        }

        // Ensure username is not null
        if (username == null || username.trim().isEmpty()) {
            username = userId;
        }

        // Ensure action is not null
        if (action == null || action.trim().isEmpty()) {
            action = "UNKNOWN_ACTION";
        }

        // Ensure entityType is not null - CRITICAL FOR ORACLE
        if (entityType == null || entityType.trim().isEmpty()) {
            entityType = "UNKNOWN_ENTITY";
        }

        // Set defaults for optional fields if needed
        if (ipAddress == null) {
            ipAddress = "0.0.0.0";
        }
    }

    /**
     * Pre-update validation
     */
    @PreUpdate
    public void preUpdate() {
        // Ensure NOT NULL fields remain not null during updates
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalStateException("userId cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("username cannot be null or empty");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalStateException("action cannot be null or empty");
        }
        if (entityType == null || entityType.trim().isEmpty()) {
            throw new IllegalStateException("entityType cannot be null or empty");
        }
    }

    /**
     * Builder class with defaults for required fields
     */
    public static class AuditLogBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private String userId = "SYSTEM";
        private String username = "SYSTEM";
        private String action = "UNKNOWN_ACTION";
        private String entityType = "UNKNOWN_ENTITY";
        private String ipAddress = "0.0.0.0";

        // Builder methods with validation
        public AuditLogBuilder userId(String userId) {
            if (userId != null && !userId.trim().isEmpty()) {
                this.userId = userId;
            }
            return this;
        }

        public AuditLogBuilder username(String username) {
            if (username != null && !username.trim().isEmpty()) {
                this.username = username;
            }
            return this;
        }

        public AuditLogBuilder action(String action) {
            if (action != null && !action.trim().isEmpty()) {
                this.action = action;
            }
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            if (entityType != null && !entityType.trim().isEmpty()) {
                this.entityType = entityType;
            }
            return this;
        }
    }

    /**
     * Factory method to create a validated AuditLog instance
     */
    public static AuditLog createValidatedAuditLog(
            String userId,
            String username,
            String action,
            String entityType,
            String entityId,
            String changes,
            String ipAddress,
            String details) {

        return AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .changes(changes)
                .ipAddress(ipAddress)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Helper method to validate if the audit log has all required fields
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
                username != null && !username.trim().isEmpty() &&
                action != null && !action.trim().isEmpty() &&
                entityType != null && !entityType.trim().isEmpty() &&
                timestamp != null;
    }

    /**
     * Returns a string representation with masked sensitive data if needed
     */
    @Override
    public String toString() {
        return String.format(
                "AuditLog{id=%d, userId='%s', username='%s', action='%s', entityType='%s', timestamp=%s}",
                id, userId, username, action, entityType, timestamp
        );
    }
}