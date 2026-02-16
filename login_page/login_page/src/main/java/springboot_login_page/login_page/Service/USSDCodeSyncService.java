// File: USSDCodeSyncService.java
package springboot_login_page.login_page.Service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot_login_page.login_page.Entity.USSDCode;
import springboot_login_page.login_page.Repository.mysql.MySQLUSSDCodeRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUSSDCodeRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLUSSDCodeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class USSDCodeSyncService {

    private static final Logger log = LoggerFactory.getLogger(USSDCodeSyncService.class);

    private final MySQLUSSDCodeRepository mysqlRepo;
    private final OracleUSSDCodeRepository oracleRepo;
    private final PostgreSQLUSSDCodeRepository postgresqlRepo;

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void syncAllDatabases() {
        log.info("Starting USSD codes synchronization across all databases");

        try {
            List<USSDCode> mysqlCodes = mysqlRepo.findAll();
            syncToOracle(mysqlCodes);
            syncToPostgreSQL(mysqlCodes);
            log.info("USSD codes synchronization completed successfully");
        } catch (Exception e) {
            log.error("USSD codes synchronization failed: {}", e.getMessage());
        }
    }

    private void syncToOracle(List<USSDCode> sourceCodes) {
        for (USSDCode code : sourceCodes) {
            try {
                oracleRepo.findByCode(code.getCode())
                        .ifPresentOrElse(
                                oracleCode -> {
                                    oracleCode.setDescription(code.getDescription());
                                    oracleCode.setActive(code.isActive());
                                    oracleRepo.save(oracleCode);
                                },
                                () -> {
                                    USSDCode newCode = new USSDCode();
                                    newCode.setCode(code.getCode());
                                    newCode.setDescription(code.getDescription());
                                    newCode.setActive(code.isActive());
                                    oracleRepo.save(newCode);
                                }
                        );
            } catch (Exception e) {
                log.error("Failed to sync code {} to Oracle: {}", code.getCode(), e.getMessage());
            }
        }
    }

    private void syncToPostgreSQL(List<USSDCode> sourceCodes) {
        for (USSDCode code : sourceCodes) {
            try {
                postgresqlRepo.findByCode(code.getCode())
                        .ifPresentOrElse(
                                postgresqlCode -> {
                                    postgresqlCode.setDescription(code.getDescription());
                                    postgresqlCode.setActive(code.isActive());
                                    postgresqlRepo.save(postgresqlCode);
                                },
                                () -> {
                                    USSDCode newCode = new USSDCode();
                                    newCode.setCode(code.getCode());
                                    newCode.setDescription(code.getDescription());
                                    newCode.setActive(code.isActive());
                                    postgresqlRepo.save(newCode);
                                }
                        );
            } catch (Exception e) {
                log.error("Failed to sync code {} to PostgreSQL: {}", code.getCode(), e.getMessage());
            }
        }
    }
}