package springboot_login_page.login_page.Repository.oracle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import springboot_login_page.login_page.Entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface OracleAuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);

    List<AuditLog> findByEntityIdOrderByTimestampDesc(String entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<AuditLog> findAllByOrderByTimestampDesc();

    // Custom save method that uses sequence
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO audit_logs (id, action, changes, details, entityid, typename, ipaddress, timestamp, userid, username) " +
            "VALUES (audit_logs_seq.NEXTVAL, :action, :changes, :details, :entityId, :entityType, :ipAddress, :timestamp, :userId, :username)",
            nativeQuery = true)
    void insertWithSequence(@Param("action") String action,
                            @Param("changes") String changes,
                            @Param("details") String details,
                            @Param("entityId") String entityId,
                            @Param("entityType") String entityType,
                            @Param("ipAddress") String ipAddress,
                            @Param("timestamp") LocalDateTime timestamp,
                            @Param("userId") String userId,
                            @Param("username") String username);
}