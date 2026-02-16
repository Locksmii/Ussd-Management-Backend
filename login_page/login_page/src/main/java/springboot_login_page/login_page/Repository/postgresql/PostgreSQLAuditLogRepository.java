// File: PostgreSQLAuditLogRepository.java
package springboot_login_page.login_page.Repository.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import springboot_login_page.login_page.Entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface PostgreSQLAuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);

    List<AuditLog> findByEntityIdOrderByTimestampDesc(String entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<AuditLog> findAllByOrderByTimestampDesc();
}