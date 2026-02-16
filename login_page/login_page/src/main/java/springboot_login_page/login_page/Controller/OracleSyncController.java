// File: OracleSyncController.java (Temporary - Delete after use)
package springboot_login_page.login_page.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot_login_page.login_page.Entity.AuditLog;
import springboot_login_page.login_page.Repository.mysql.MySQLAuditLogRepository;
import springboot_login_page.login_page.Repository.oracle.OracleAuditLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin/fix")
@RequiredArgsConstructor
public class OracleSyncController {

    private final MySQLAuditLogRepository mysqlAuditRepo;
    private final OracleAuditLogRepository oracleAuditRepo;

    @PostMapping("/sync-oracle")
    public ResponseEntity<String> syncOracle() {
        try {
            List<AuditLog> mysqlLogs = mysqlAuditRepo.findAll();
            int synced = 0;

            for (AuditLog log : mysqlLogs) {
                if (!oracleAuditRepo.existsById(log.getId())) {
                    AuditLog oracleLog = new AuditLog();
                    oracleLog.setId(log.getId());
                    oracleLog.setUserId(log.getUserId());
                    oracleLog.setUsername(log.getUsername());
                    oracleLog.setAction(log.getAction());
                    oracleLog.setEntityType(log.getEntityType());
                    oracleLog.setEntityId(log.getEntityId());
                    oracleLog.setChanges(log.getChanges());
                    oracleLog.setIpAddress(log.getIpAddress());
                    oracleLog.setDetails(log.getDetails());
                    oracleLog.setTimestamp(log.getTimestamp());

                    oracleAuditRepo.save(oracleLog);
                    synced++;
                }
            }

            return ResponseEntity.ok("Successfully synced " + synced + " audit logs to Oracle");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}