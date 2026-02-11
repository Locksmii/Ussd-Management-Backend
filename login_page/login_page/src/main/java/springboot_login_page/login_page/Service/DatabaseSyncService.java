package springboot_login_page.login_page.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.mysql.MySQLUserRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseSyncService {

    private final MySQLUserRepository mysqlUserRepository;
    private final OracleUserRepository oracleUserRepository;

    /**
     * Synchronize users from MySQL to Oracle
     * This can be called manually or scheduled
     */
    @Transactional
    public void syncUsersToOracle() {
        List<User> mysqlUsers = mysqlUserRepository.findAll();

        for (User mysqlUser : mysqlUsers) {
            // Check if user exists in Oracle
            oracleUserRepository.findByUsername(mysqlUser.getUsername())
                    .ifPresentOrElse(
                            // Update existing user
                            oracleUser -> {
                                oracleUser.setPassword(mysqlUser.getPassword());
                                oracleUser.setRole(mysqlUser.getRole());
                                oracleUserRepository.save(oracleUser);
                            },
                            // Create new user if doesn't exist
                            () -> {
                                User newOracleUser = new User();
                                newOracleUser.setUsername(mysqlUser.getUsername());
                                newOracleUser.setPassword(mysqlUser.getPassword());
                                newOracleUser.setRole(mysqlUser.getRole());
                                // Note: We're not copying ID to avoid conflicts
                                oracleUserRepository.save(newOracleUser);
                            }
                    );
        }
        System.out.println("Users synchronized from MySQL to Oracle");
    }

    /**
     * Synchronize users from Oracle to MySQL
     */
    @Transactional
    public void syncUsersToMySQL() {
        List<User> oracleUsers = oracleUserRepository.findAll();

        for (User oracleUser : oracleUsers) {
            mysqlUserRepository.findByUsername(oracleUser.getUsername())
                    .ifPresentOrElse(
                            mysqlUser -> {
                                mysqlUser.setPassword(oracleUser.getPassword());
                                mysqlUser.setRole(oracleUser.getRole());
                                mysqlUserRepository.save(mysqlUser);
                            },
                            () -> {
                                User newMysqlUser = new User();
                                newMysqlUser.setUsername(oracleUser.getUsername());
                                newMysqlUser.setPassword(oracleUser.getPassword());
                                newMysqlUser.setRole(oracleUser.getRole());
                                mysqlUserRepository.save(newMysqlUser);
                            }
                    );
        }
        System.out.println("Users synchronized from Oracle to MySQL");
    }

    /**
     * Bi-directional synchronization
     */
    @Transactional
    public void syncBothDatabases() {
        syncUsersToOracle();
        syncUsersToMySQL();
    }

    /**
     * Scheduled synchronization every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void scheduledSync() {
        try {
            syncBothDatabases();
        } catch (Exception e) {
            System.err.println("Database synchronization failed: " + e.getMessage());
        }
    }
}