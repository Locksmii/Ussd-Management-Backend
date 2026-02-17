package springboot_login_page.login_page.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springboot_login_page.login_page.Entity.AuditLog;
import springboot_login_page.login_page.Repository.mysql.MySQLAuditLogRepository;
import springboot_login_page.login_page.Repository.oracle.OracleAuditLogRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLAuditLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MySQLAuditLogRepository mysqlAuditRepo;
    private final OracleAuditLogRepository oracleAuditRepo;
    private final PostgreSQLAuditLogRepository postgresqlAuditRepo;

    @Transactional
    public void logCreate(String entityType, String entityId, Object entity, String username) {
        AuditLog auditLog = createAuditLog("CREATE", entityType, entityId, entity, null, username);
        saveToAllDatabases(auditLog, "CREATE");
    }

    @Transactional
    public void logUpdate(String entityType, String entityId, Object before, Object after, String username) {
        Map<String, Object> changes = getChanges(before, after);
        AuditLog auditLog = createAuditLog("UPDATE", entityType, entityId, after, changes, username);
        saveToAllDatabases(auditLog, "UPDATE");
    }

    @Transactional
    public void logDelete(String entityType, String entityId, Object entity, String username) {
        AuditLog auditLog = createAuditLog("DELETE", entityType, entityId, entity, null, username);
        saveToAllDatabases(auditLog, "DELETE");
    }

    private AuditLog createAuditLog(String action, String entityType, String entityId,
                                    Object entity, Map<String, Object> changes, String username) {
        AuditLog auditLog = new AuditLog();

        // Ensure userId is never null - use username as userId
        String userId = (username != null && !username.isEmpty()) ? username : "system";
        auditLog.setUserId(userId);
        auditLog.setUsername(userId); // Username should match userId for consistency

        auditLog.setAction(action);
        auditLog.setEntityType(entityType.toUpperCase());
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(getClientIp());

        try {
            if (changes != null) {
                auditLog.setChanges(objectMapper.writeValueAsString(changes));
            } else {
                Map<String, Object> entityMap = new HashMap<>();
                entityMap.put(action.toLowerCase(), convertToMap(entity));
                auditLog.setChanges(objectMapper.writeValueAsString(entityMap));
            }
        } catch (JsonProcessingException e) {
            auditLog.setChanges("{\"error\": \"Failed to serialize data\"}");
            log.error("Failed to serialize audit data for action {} by user {}: {}",
                    action, userId, e.getMessage());
        }

        // Add timestamp if not set by @CreationTimestamp
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }

        return auditLog;
    }

    private void saveToAllDatabases(AuditLog auditLog, String action) {
        AtomicInteger successCount = new AtomicInteger(0);

        // Validate and fix required fields
        if (auditLog.getUserId() == null || auditLog.getUserId().trim().isEmpty()) {
            String username = auditLog.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                auditLog.setUserId(username);
                log.warn("Fixed null userId by using username: {}", username);
            } else {
                auditLog.setUserId("SYSTEM");
                log.warn("Fixed null userId by using 'SYSTEM'");
            }
        }

        if (auditLog.getUsername() == null || auditLog.getUsername().trim().isEmpty()) {
            auditLog.setUsername(auditLog.getUserId());
            log.warn("Fixed null username by using userId: {}", auditLog.getUserId());
        }

        if (auditLog.getEntityType() == null || auditLog.getEntityType().trim().isEmpty()) {
            auditLog.setEntityType("UNKNOWN_ENTITY");
            log.warn("Fixed null entityType by using 'UNKNOWN_ENTITY'");
        }

        if (auditLog.getAction() == null || auditLog.getAction().trim().isEmpty()) {
            auditLog.setAction(action);
            log.warn("Fixed null action by using: {}", action);
        }

        log.debug("Attempting to save audit log - Action: {}, User: {}, Entity: {}",
                action, auditLog.getUserId(), auditLog.getEntityType());

        // Save to MySQL
        try {
            mysqlAuditRepo.save(auditLog);
            successCount.incrementAndGet();
            log.debug("Audit log saved to MySQL - ID: {}, User: {}", auditLog.getId(), auditLog.getUserId());
        } catch (Exception e) {
            log.error("Failed to save audit log to MySQL: {}", e.getMessage());
        }

        // Save to Oracle using native query
        try {
            AuditLog oracleLog = copyAuditLog(auditLog);
            oracleAuditRepo.insertWithSequence(
                    oracleLog.getAction(),
                    oracleLog.getChanges(),
                    oracleLog.getDetails(),
                    oracleLog.getEntityId(),
                    oracleLog.getEntityType(),
                    oracleLog.getIpAddress(),
                    oracleLog.getTimestamp(),
                    oracleLog.getUserId(),
                    oracleLog.getUsername()
            );
            successCount.incrementAndGet();
            log.debug("Audit log saved to Oracle - User: {}", oracleLog.getUserId());
        } catch (Exception e) {
            log.error("Failed to save audit log to Oracle: {}", e.getMessage());
        }

        // Save to PostgreSQL
        try {
            AuditLog postgresqlLog = copyAuditLog(auditLog);
            // Clear ID to let sequence generate it
            postgresqlLog.setId(null);
            postgresqlAuditRepo.save(postgresqlLog);
            successCount.incrementAndGet();
            log.debug("Audit log saved to PostgreSQL - User: {}", postgresqlLog.getUserId());
        } catch (Exception e) {
            log.error("Failed to save audit log to PostgreSQL: {}", e.getMessage());
        }

        if (successCount.get() == 0) {
            log.error("CRITICAL: Failed to save audit log to ANY database! Action: {} by user: {}",
                    action, auditLog.getUserId());
        } else {
            log.info("Audit log saved to {} database(s) - Action: {} by: {}",
                    successCount.get(), action, auditLog.getUserId());
        }
    }

    private AuditLog copyAuditLog(AuditLog original) {
        AuditLog copy = new AuditLog();
        copy.setUserId(original.getUserId());
        copy.setUsername(original.getUsername());
        copy.setAction(original.getAction());
        copy.setEntityType(original.getEntityType());
        copy.setEntityId(original.getEntityId());
        copy.setChanges(original.getChanges());
        copy.setIpAddress(original.getIpAddress());
        copy.setDetails(original.getDetails());
        copy.setTimestamp(original.getTimestamp() != null ? original.getTimestamp() : LocalDateTime.now());
        return copy;
    }

    public List<AuditLog> getAllAuditLogs() {
        try {
            List<AuditLog> logs = mysqlAuditRepo.findAllByOrderByTimestampDesc();
            log.info("Retrieved {} audit logs from MySQL", logs.size());
            return logs;
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                List<AuditLog> logs = oracleAuditRepo.findAllByOrderByTimestampDesc();
                log.info("Retrieved {} audit logs from Oracle", logs.size());
                return logs;
            } catch (Exception ex) {
                log.warn("Oracle audit unavailable, trying PostgreSQL: {}", ex.getMessage());
                List<AuditLog> logs = postgresqlAuditRepo.findAllByOrderByTimestampDesc();
                log.info("Retrieved {} audit logs from PostgreSQL", logs.size());
                return logs;
            }
        }
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        String type = entityType.toUpperCase();
        try {
            return mysqlAuditRepo.findByEntityTypeOrderByTimestampDesc(type);
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                return oracleAuditRepo.findByEntityTypeOrderByTimestampDesc(type);
            } catch (Exception ex) {
                return postgresqlAuditRepo.findByEntityTypeOrderByTimestampDesc(type);
            }
        }
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        String act = action.toUpperCase();
        try {
            return mysqlAuditRepo.findByActionOrderByTimestampDesc(act);
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                return oracleAuditRepo.findByActionOrderByTimestampDesc(act);
            } catch (Exception ex) {
                return postgresqlAuditRepo.findByActionOrderByTimestampDesc(act);
            }
        }
    }

    public List<AuditLog> getAuditLogsByUser(String userId) {
        try {
            return mysqlAuditRepo.findByUserIdOrderByTimestampDesc(userId);
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                return oracleAuditRepo.findByUserIdOrderByTimestampDesc(userId);
            } catch (Exception ex) {
                return postgresqlAuditRepo.findByUserIdOrderByTimestampDesc(userId);
            }
        }
    }

    public List<AuditLog> getAuditLogsForEntity(String entityType, String entityId) {
        String type = entityType.toUpperCase();
        try {
            return mysqlAuditRepo.findByEntityIdOrderByTimestampDesc(entityId)
                    .stream()
                    .filter(log -> log.getEntityType().equalsIgnoreCase(type))
                    .toList();
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                return oracleAuditRepo.findByEntityIdOrderByTimestampDesc(entityId)
                        .stream()
                        .filter(log -> log.getEntityType().equalsIgnoreCase(type))
                        .toList();
            } catch (Exception ex) {
                return postgresqlAuditRepo.findByEntityIdOrderByTimestampDesc(entityId)
                        .stream()
                        .filter(log -> log.getEntityType().equalsIgnoreCase(type))
                        .toList();
            }
        }
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return mysqlAuditRepo.findByDateRange(start, end);
        } catch (Exception e) {
            log.warn("MySQL audit unavailable, trying Oracle: {}", e.getMessage());
            try {
                return oracleAuditRepo.findByDateRange(start, end);
            } catch (Exception ex) {
                return postgresqlAuditRepo.findByDateRange(start, end);
            }
        }
    }

    private Map<String, Object> getChanges(Object before, Object after) {
        Map<String, Object> beforeMap = convertToMap(before);
        Map<String, Object> afterMap = convertToMap(after);
        Map<String, Object> changes = new HashMap<>();

        // Remove sensitive or non-comparable fields
        beforeMap.remove("id");
        beforeMap.remove("timestamp");
        beforeMap.remove("password");
        afterMap.remove("id");
        afterMap.remove("timestamp");
        afterMap.remove("password");

        for (String key : afterMap.keySet()) {
            if (!beforeMap.containsKey(key)) {
                changes.put(key, Map.of("new", afterMap.get(key)));
            } else if (beforeMap.get(key) != null && !beforeMap.get(key).equals(afterMap.get(key))) {
                changes.put(key, Map.of(
                        "old", beforeMap.get(key),
                        "new", afterMap.get(key)
                ));
            } else if (beforeMap.get(key) == null && afterMap.get(key) != null) {
                changes.put(key, Map.of(
                        "old", null,
                        "new", afterMap.get(key)
                ));
            }
        }

        // Also check for fields that were removed
        for (String key : beforeMap.keySet()) {
            if (!afterMap.containsKey(key) && beforeMap.get(key) != null) {
                changes.put(key, Map.of(
                        "old", beforeMap.get(key),
                        "new", null
                ));
            }
        }

        return changes;
    }

    private Map<String, Object> convertToMap(Object obj) {
        try {
            if (obj == null) return new HashMap<>();
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to map: {}", e.getMessage());
            return Map.of("error", "Failed to convert object");
        }
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // Handle multiple IPs in X-Forwarded-For
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip;
        } catch (Exception e) {
            log.debug("Could not get client IP: {}", e.getMessage());
            return "0.0.0.0"; // Return a default IP instead of "system-internal"
        }
    }
}