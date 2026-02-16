// File: USSDCodeService.java (Updated with Audit Logging)
package springboot_login_page.login_page.Service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot_login_page.login_page.Entity.USSDCode;
import springboot_login_page.login_page.Repository.mysql.MySQLUSSDCodeRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUSSDCodeRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLUSSDCodeRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class USSDCodeService {

    private static final Logger log = LoggerFactory.getLogger(USSDCodeService.class);

    private final MySQLUSSDCodeRepository mysqlRepo;
    private final OracleUSSDCodeRepository oracleRepo;
    private final PostgreSQLUSSDCodeRepository postgresqlRepo;
    private final AuditService auditService; // ADD THIS

    @Transactional
    public USSDCode createUSSDCode(USSDCode code) {
        log.info("Creating USSD code: {}", code.getCode());

        if (!isValidUSSDCode(code.getCode())) {
            throw new RuntimeException("Invalid USSD code format. Must be like *123# or *123*456#");
        }

        if (mysqlRepo.findByCode(code.getCode()).isPresent()) {
            throw new RuntimeException("USSD code already exists");
        }

        // Use AtomicInteger for mutable counter in lambdas
        AtomicInteger successCount = new AtomicInteger(0);
        USSDCode savedCode = null;

        try {
            savedCode = mysqlRepo.save(code);
            successCount.incrementAndGet();
            log.info("Saved to MySQL database");
        } catch (Exception e) {
            log.error("Failed to save to MySQL: {}", e.getMessage());
        }

        try {
            USSDCode oracleCode = new USSDCode();
            oracleCode.setCode(code.getCode());
            oracleCode.setDescription(code.getDescription());
            oracleCode.setActive(code.isActive());
            oracleRepo.save(oracleCode);
            successCount.incrementAndGet();
            log.info("Saved to Oracle database");
        } catch (Exception e) {
            log.error("Failed to save to Oracle: {}", e.getMessage());
        }

        try {
            USSDCode postgresqlCode = new USSDCode();
            postgresqlCode.setCode(code.getCode());
            postgresqlCode.setDescription(code.getDescription());
            postgresqlCode.setActive(code.isActive());
            postgresqlRepo.save(postgresqlCode);
            successCount.incrementAndGet();
            log.info("Saved to PostgreSQL database");
        } catch (Exception e) {
            log.error("Failed to save to PostgreSQL: {}", e.getMessage());
        }

        if (successCount.get() == 0) {
            throw new RuntimeException("Failed to save USSD code to any database");
        }

        // ADD AUDIT LOGGING HERE
        try {
            String username = getCurrentUsername();
            auditService.logCreate("USSD_CODE", savedCode != null ?
                    savedCode.getId().toString() : "NEW", code, username);
            log.info("Audit log created for USSD code creation");
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }

        log.info("USSD code created successfully in {} database(s)", successCount.get());
        return savedCode != null ? savedCode : code;
    }

    @Transactional
    public void updateUSSDCode(Long id, USSDCode updatedCode) {
        log.info("Updating USSD code with id: {}", id);

        USSDCode existingCode = mysqlRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("USSD code not found"));

        String originalCode = existingCode.getCode();
        String newCode = updatedCode.getCode();

        if (!originalCode.equals(newCode) && mysqlRepo.findByCode(newCode).isPresent()) {
            throw new RuntimeException("USSD code " + newCode + " already exists");
        }

        // Store a copy of the original state for audit
        USSDCode beforeState = new USSDCode();
        beforeState.setCode(existingCode.getCode());
        beforeState.setDescription(existingCode.getDescription());
        beforeState.setActive(existingCode.isActive());

        AtomicInteger successCount = new AtomicInteger(0);

        try {
            existingCode.setCode(updatedCode.getCode());
            existingCode.setDescription(updatedCode.getDescription());
            existingCode.setActive(updatedCode.isActive());
            mysqlRepo.save(existingCode);
            successCount.incrementAndGet();
            log.info("Updated in MySQL");
        } catch (Exception e) {
            log.error("Failed to update MySQL: {}", e.getMessage());
        }

        // Use a final variable for the code value
        final String finalOriginalCode = originalCode;

        try {
            oracleRepo.findByCode(finalOriginalCode).ifPresent(oracleCode -> {
                oracleCode.setCode(updatedCode.getCode());
                oracleCode.setDescription(updatedCode.getDescription());
                oracleCode.setActive(updatedCode.isActive());
                oracleRepo.save(oracleCode);
                successCount.incrementAndGet();
                log.info("Updated in Oracle");
            });
        } catch (Exception e) {
            log.error("Failed to update Oracle: {}", e.getMessage());
        }

        try {
            postgresqlRepo.findByCode(finalOriginalCode).ifPresent(postgresqlCode -> {
                postgresqlCode.setCode(updatedCode.getCode());
                postgresqlCode.setDescription(updatedCode.getDescription());
                postgresqlCode.setActive(updatedCode.isActive());
                postgresqlRepo.save(postgresqlCode);
                successCount.incrementAndGet();
                log.info("Updated in PostgreSQL");
            });
        } catch (Exception e) {
            log.error("Failed to update PostgreSQL: {}", e.getMessage());
        }

        if (successCount.get() == 0) {
            throw new RuntimeException("Failed to update USSD code in any database");
        }

        // ADD AUDIT LOGGING HERE
        try {
            String username = getCurrentUsername();
            auditService.logUpdate("USSD_CODE", id.toString(), beforeState, updatedCode, username);
            log.info("Audit log created for USSD code update");
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }

        log.info("USSD code updated successfully in {} database(s)", successCount.get());
    }

    @Transactional
    public void deleteUSSDCode(Long id) {
        log.info("Deleting USSD code with id: {}", id);

        USSDCode code = mysqlRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("USSD code not found"));

        // Store a copy for audit before deletion
        USSDCode beforeDelete = new USSDCode();
        beforeDelete.setCode(code.getCode());
        beforeDelete.setDescription(code.getDescription());
        beforeDelete.setActive(code.isActive());

        String deletedCode = code.getCode();
        AtomicInteger successCount = new AtomicInteger(0);

        try {
            mysqlRepo.deleteById(id);
            successCount.incrementAndGet();
            log.info("Deleted from MySQL");
        } catch (Exception e) {
            log.error("Failed to delete from MySQL: {}", e.getMessage());
        }

        final String finalDeletedCode = deletedCode;

        try {
            oracleRepo.findByCode(finalDeletedCode).ifPresent(oracleCode -> {
                oracleRepo.delete(oracleCode);
                successCount.incrementAndGet();
                log.info("Deleted from Oracle");
            });
        } catch (Exception e) {
            log.error("Failed to delete from Oracle: {}", e.getMessage());
        }

        try {
            postgresqlRepo.findByCode(finalDeletedCode).ifPresent(postgresqlCode -> {
                postgresqlRepo.delete(postgresqlCode);
                successCount.incrementAndGet();
                log.info("Deleted from PostgreSQL");
            });
        } catch (Exception e) {
            log.error("Failed to delete from PostgreSQL: {}", e.getMessage());
        }

        if (successCount.get() == 0) {
            throw new RuntimeException("Failed to delete USSD code from any database");
        }

        // ADD AUDIT LOGGING HERE
        try {
            String username = getCurrentUsername();
            auditService.logDelete("USSD_CODE", id.toString(), beforeDelete, username);
            log.info("Audit log created for USSD code deletion");
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }

        log.info("USSD code deleted successfully from {} database(s)", successCount.get());
    }

    @Transactional(readOnly = true)
    public List<USSDCode> getAllUSSDCodes() {
        log.info("Fetching all USSD codes");

        try {
            List<USSDCode> codes = mysqlRepo.findAll();
            log.info("Retrieved {} codes from MySQL", codes.size());
            return codes;
        } catch (Exception e) {
            log.warn("MySQL unavailable, trying Oracle: {}", e.getMessage());
            try {
                List<USSDCode> codes = oracleRepo.findAll();
                log.info("Retrieved {} codes from Oracle", codes.size());
                return codes;
            } catch (Exception ex) {
                log.warn("Oracle unavailable, trying PostgreSQL: {}", ex.getMessage());
                List<USSDCode> codes = postgresqlRepo.findAll();
                log.info("Retrieved {} codes from PostgreSQL", codes.size());
                return codes;
            }
        }
    }

    @Transactional(readOnly = true)
    public USSDCode getUSSDCodeById(Long id) {
        log.info("Fetching USSD code with id: {}", id);

        return mysqlRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("USSD code not found with id: " + id));
    }

    private boolean isValidUSSDCode(String code) {
        return code != null && code.matches("^\\*[0-9\\*]+#$");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}