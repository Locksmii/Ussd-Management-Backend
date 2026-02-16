// File: AuditLog.java
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Lob
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        if (userId == null || userId.trim().isEmpty()) {
            if (username != null && !username.trim().isEmpty()) {
                userId = username;
            } else {
                userId = "SYSTEM";
            }
        }

        if (username == null || username.trim().isEmpty()) {
            username = userId;
        }

        if (action == null || action.trim().isEmpty()) {
            action = "UNKNOWN_ACTION";
        }

        if (entityType == null || entityType.trim().isEmpty()) {
            entityType = "UNKNOWN_ENTITY";
        }

        if (ipAddress == null) {
            ipAddress = "0.0.0.0";
        }
    }

    @PreUpdate
    public void preUpdate() {
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
}