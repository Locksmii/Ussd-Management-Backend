package springboot_login_page.login_page.Service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot_login_page.login_page.Entity.AuditLog;
import springboot_login_page.login_page.Repository.mysql.MySQLAuditLogRepository;
import springboot_login_page.login_page.Repository.oracle.OracleAuditLogRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLAuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditSyncService {

    private static final Logger log = LoggerFactory.getLogger(AuditSyncService.class);

    private final MySQLAuditLogRepository mysqlAuditRepo;
    private final OracleAuditLogRepository oracleAuditRepo;
    private final PostgreSQLAuditLogRepository postgresqlAuditRepo;

    @Scheduled(fixedDelay = 3600000) // Run every hour
    @Transactional
    public void syncAuditLogs() {
        log.info("Starting audit logs synchronization across all databases");

        try {
            // Sync from MySQL to others
            List<AuditLog> mysqlLogs = mysqlAuditRepo.findAll();
            log.info("Found {} audit logs in MySQL to sync", mysqlLogs.size());

            if (!mysqlLogs.isEmpty()) {
                // Log sample of records to debug
                for (int i = 0; i < Math.min(3, mysqlLogs.size()); i++) {
                    AuditLog sampleLog = mysqlLogs.get(i);
                    log.info("Sample MySQL record {} - ID: {}, UserId: '{}', Username: '{}', EntityType: '{}', Action: '{}'",
                            i + 1, sampleLog.getId(), sampleLog.getUserId(),
                            sampleLog.getUsername(), sampleLog.getEntityType(), sampleLog.getAction());
                }

                syncToOracle(mysqlLogs, "MySQL");
                syncToPostgreSQL(mysqlLogs, "MySQL");
            }

            // Sync from Oracle to others
            List<AuditLog> oracleLogs = oracleAuditRepo.findAll();
            log.info("Found {} audit logs in Oracle to sync", oracleLogs.size());

            if (!oracleLogs.isEmpty()) {
                syncToMySQL(oracleLogs, "Oracle");
                syncToPostgreSQL(oracleLogs, "Oracle");
            }

            // Sync from PostgreSQL to others
            List<AuditLog> postgresqlLogs = postgresqlAuditRepo.findAll();
            log.info("Found {} audit logs in PostgreSQL to sync", postgresqlLogs.size());

            if (!postgresqlLogs.isEmpty()) {
                syncToMySQL(postgresqlLogs, "PostgreSQL");
                syncToOracle(postgresqlLogs, "PostgreSQL");
            }

            log.info("Audit logs synchronization completed successfully");
        } catch (Exception e) {
            log.error("Audit logs synchronization failed: {}", e.getMessage(), e);
        }
    }

    private void syncToOracle(List<AuditLog> sourceLogs, String sourceDb) {
        int synced = 0;
        int failed = 0;

        for (AuditLog auditLog : sourceLogs) {
            try {
                if (!oracleAuditRepo.existsById(auditLog.getId())) {
                    log.info("Attempting to save to Oracle from {} - ID: {}, userId: '{}', username: '{}', entityType: '{}'",
                            sourceDb, auditLog.getId(), auditLog.getUserId(),
                            auditLog.getUsername(), auditLog.getEntityType());

                    // Validate and prepare the audit log for Oracle (all NOT NULL constraints)
                    AuditLog validatedLog = validateForOracle(auditLog, sourceDb);

                    // Use the native query with sequence
                    oracleAuditRepo.insertWithSequence(
                            validatedLog.getAction(),           // NOT NULL
                            validatedLog.getChanges(),          // Can be null (TEXT)
                            validatedLog.getDetails(),          // Can be null (TEXT)
                            validatedLog.getEntityId(),         // Can be null
                            validatedLog.getEntityType(),       // NOT NULL - FIXED
                            validatedLog.getIpAddress(),        // Can be null
                            validatedLog.getTimestamp(),        // NOT NULL
                            validatedLog.getUserId(),           // NOT NULL
                            validatedLog.getUsername()          // NOT NULL
                    );

                    synced++;
                    log.info("Successfully synced audit log {} to Oracle from {}", auditLog.getId(), sourceDb);
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed to sync audit log {} to Oracle from {}: {}",
                        auditLog.getId(), sourceDb, e.getMessage(), e);

                // Log the actual values that caused the issue
                log.error("Audit log data - ID: {}, userId: '{}', username: '{}', entityType: '{}', action: '{}', timestamp: {}",
                        auditLog.getId(), auditLog.getUserId(), auditLog.getUsername(),
                        auditLog.getEntityType(), auditLog.getAction(), auditLog.getTimestamp());

                if (e.getMessage() != null && e.getMessage().contains("ORA-01400")) {
                    log.error("NULL value detected in NOT NULL column. Check these fields must not be null: " +
                            "action, entityType, timestamp, userId, username");
                }
            }
        }
        log.info("Synced {} audit logs to Oracle from {} ({} failed)", synced, sourceDb, failed);
    }

    private void syncToPostgreSQL(List<AuditLog> sourceLogs, String sourceDb) {
        int synced = 0;
        int failed = 0;

        for (AuditLog auditLog : sourceLogs) {
            try {
                if (!postgresqlAuditRepo.existsById(auditLog.getId())) {
                    AuditLog postgresqlLog = validateAuditLog(auditLog, sourceDb);
                    postgresqlAuditRepo.save(postgresqlLog);
                    synced++;
                    log.debug("Synced audit log {} to PostgreSQL from {}", auditLog.getId(), sourceDb);
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed to sync audit log {} to PostgreSQL from {}: {}",
                        auditLog.getId(), sourceDb, e.getMessage());
            }
        }
        log.info("Synced {} audit logs to PostgreSQL from {} ({} failed)", synced, sourceDb, failed);
    }

    private void syncToMySQL(List<AuditLog> sourceLogs, String sourceDb) {
        int synced = 0;
        int failed = 0;

        for (AuditLog auditLog : sourceLogs) {
            try {
                if (!mysqlAuditRepo.existsById(auditLog.getId())) {
                    AuditLog mysqlLog = validateAuditLog(auditLog, sourceDb);
                    mysqlAuditRepo.save(mysqlLog);
                    synced++;
                    log.debug("Synced audit log {} to MySQL from {}", auditLog.getId(), sourceDb);
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed to sync audit log {} to MySQL from {}: {}",
                        auditLog.getId(), sourceDb, e.getMessage());
            }
        }
        log.info("Synced {} audit logs to MySQL from {} ({} failed)", synced, sourceDb, failed);
    }

    /**
     * Special validation for Oracle which has NOT NULL constraints on multiple fields
     * Based on your AuditLog entity definition
     */
    private AuditLog validateForOracle(AuditLog original, String sourceDb) {
        AuditLog validated = new AuditLog();

        validated.setId(original.getId());

        // CRITICAL FIX: Ensure entityType is NEVER null (matches @Column(nullable=false) in entity)
        String entityType = original.getEntityType();
        if (entityType == null || entityType.trim().isEmpty()) {
            entityType = "UNKNOWN_ENTITY";
            log.warn("entityType was null/empty for audit log {} from {}, setting to '{}'",
                    original.getId(), sourceDb, entityType);
        }
        validated.setEntityType(entityType);

        // Ensure action is NEVER null (matches @Column(nullable=false) in entity)
        String action = original.getAction();
        if (action == null || action.trim().isEmpty()) {
            action = "UNKNOWN_ACTION";
            log.warn("action was null/empty for audit log {} from {}, setting to '{}'",
                    original.getId(), sourceDb, action);
        }
        validated.setAction(action);

        // Ensure userId is NEVER null (matches @Column(nullable=false) in entity)
        String userId = original.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            String username = original.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                userId = username;
                log.debug("Using username '{}' as userId for audit log {} from {}",
                        username, original.getId(), sourceDb);
            } else {
                userId = "SYSTEM";
                log.warn("Both userId and username are null/empty for audit log {} from {}, using '{}'",
                        original.getId(), sourceDb, userId);
            }
        }
        validated.setUserId(userId);

        // Ensure username is NEVER null (matches @Column(nullable=false) in entity)
        String username = original.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = userId;
            log.debug("Username was null/empty, using userId '{}' for audit log {} from {}",
                    userId, original.getId(), sourceDb);
        }
        validated.setUsername(username);

        // Ensure timestamp is NEVER null (matches @Column(nullable=false) in entity)
        LocalDateTime timestamp = original.getTimestamp();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
            log.warn("timestamp was null for audit log {} from {}, setting to current time",
                    original.getId(), sourceDb);
        }
        validated.setTimestamp(timestamp);

        // Optional fields - can be null
        validated.setEntityId(original.getEntityId());
        validated.setChanges(original.getChanges());
        validated.setIpAddress(original.getIpAddress());
        validated.setDetails(original.getDetails());

        return validated;
    }

    /**
     * Generic validation for MySQL and PostgreSQL
     * Based on your AuditLog entity definition
     */
    private AuditLog validateAuditLog(AuditLog original, String sourceDb) {
        AuditLog validated = new AuditLog();

        validated.setId(original.getId());

        // Ensure all NOT NULL fields have values

        // EntityType validation
        String entityType = original.getEntityType();
        if (entityType == null || entityType.trim().isEmpty()) {
            entityType = "UNKNOWN_ENTITY";
            log.warn("entityType was null/empty for audit log {} from {}, setting to '{}'",
                    original.getId(), sourceDb, entityType);
        }
        validated.setEntityType(entityType);

        // Action validation
        String action = original.getAction();
        if (action == null || action.trim().isEmpty()) {
            action = "UNKNOWN_ACTION";
            log.warn("action was null/empty for audit log {} from {}, setting to '{}'",
                    original.getId(), sourceDb, action);
        }
        validated.setAction(action);

        // UserId validation
        String userId = original.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            String username = original.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                userId = username;
                log.debug("Using username '{}' as userId for audit log {} from {}",
                        username, original.getId(), sourceDb);
            } else {
                userId = "system";
                log.warn("Both userId and username are null/empty for audit log {} from {}, using '{}'",
                        original.getId(), sourceDb, userId);
            }
        }
        validated.setUserId(userId);

        // Username validation
        String username = original.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = userId;
            log.debug("Username was null/empty, using userId '{}' for audit log {} from {}",
                    userId, original.getId(), sourceDb);
        }
        validated.setUsername(username);

        // Timestamp validation
        LocalDateTime timestamp = original.getTimestamp();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
            log.warn("timestamp was null for audit log {} from {}, setting to current time",
                    original.getId(), sourceDb);
        }
        validated.setTimestamp(timestamp);

        // Optional fields - preserve original values (can be null)
        validated.setEntityId(original.getEntityId());
        validated.setChanges(original.getChanges());
        validated.setIpAddress(original.getIpAddress());
        validated.setDetails(original.getDetails());

        return validated;
    }
}