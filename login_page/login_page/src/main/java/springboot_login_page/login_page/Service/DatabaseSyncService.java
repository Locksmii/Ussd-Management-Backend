// File: DatabaseSyncService.java
package springboot_login_page.login_page.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.mysql.MySQLUserRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUserRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseSyncService {

    private final MySQLUserRepository mysqlUserRepository;
    private final OracleUserRepository oracleUserRepository;
    private final PostgreSQLUserRepository postgreSQLUserRepository;

    @Transactional
    public void syncUsersToOracleAndPostgreSQL() {
        List<User> mysqlUsers = mysqlUserRepository.findAll();

        for (User mysqlUser : mysqlUsers) {
            oracleUserRepository.findByUsername(mysqlUser.getUsername())
                    .ifPresentOrElse(
                            oracleUser -> {
                                oracleUser.setPassword(mysqlUser.getPassword());
                                oracleUser.setRole(mysqlUser.getRole());
                                oracleUserRepository.save(oracleUser);
                            },
                            () -> {
                                User newOracleUser = new User();
                                newOracleUser.setUsername(mysqlUser.getUsername());
                                newOracleUser.setPassword(mysqlUser.getPassword());
                                newOracleUser.setRole(mysqlUser.getRole());
                                oracleUserRepository.save(newOracleUser);
                            }
                    );

            postgreSQLUserRepository.findByUsername(mysqlUser.getUsername())
                    .ifPresentOrElse(
                            postgresqlUser -> {
                                postgresqlUser.setPassword(mysqlUser.getPassword());
                                postgresqlUser.setRole(mysqlUser.getRole());
                                postgreSQLUserRepository.save(postgresqlUser);
                            },
                            () -> {
                                User newPostgreSQLUser = new User();
                                newPostgreSQLUser.setUsername(mysqlUser.getUsername());
                                newPostgreSQLUser.setPassword(mysqlUser.getPassword());
                                newPostgreSQLUser.setRole(mysqlUser.getRole());
                                postgreSQLUserRepository.save(newPostgreSQLUser);
                            }
                    );
        }
        System.out.println("Users synchronized from MySQL to Oracle and PostgreSQL");
    }

    @Transactional
    public void syncUsersToMySQLAndPostgreSQL() {
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

            postgreSQLUserRepository.findByUsername(oracleUser.getUsername())
                    .ifPresentOrElse(
                            postgresqlUser -> {
                                postgresqlUser.setPassword(oracleUser.getPassword());
                                postgresqlUser.setRole(oracleUser.getRole());
                                postgreSQLUserRepository.save(postgresqlUser);
                            },
                            () -> {
                                User newPostgreSQLUser = new User();
                                newPostgreSQLUser.setUsername(oracleUser.getUsername());
                                newPostgreSQLUser.setPassword(oracleUser.getPassword());
                                newPostgreSQLUser.setRole(oracleUser.getRole());
                                postgreSQLUserRepository.save(newPostgreSQLUser);
                            }
                    );
        }
        System.out.println("Users synchronized from Oracle to MySQL and PostgreSQL");
    }

    @Transactional
    public void syncUsersFromPostgreSQLToOthers() {
        List<User> postgresqlUsers = postgreSQLUserRepository.findAll();

        for (User postgresqlUser : postgresqlUsers) {
            mysqlUserRepository.findByUsername(postgresqlUser.getUsername())
                    .ifPresentOrElse(
                            mysqlUser -> {
                                mysqlUser.setPassword(postgresqlUser.getPassword());
                                mysqlUser.setRole(postgresqlUser.getRole());
                                mysqlUserRepository.save(mysqlUser);
                            },
                            () -> {
                                User newMysqlUser = new User();
                                newMysqlUser.setUsername(postgresqlUser.getUsername());
                                newMysqlUser.setPassword(postgresqlUser.getPassword());
                                newMysqlUser.setRole(postgresqlUser.getRole());
                                mysqlUserRepository.save(newMysqlUser);
                            }
                    );

            oracleUserRepository.findByUsername(postgresqlUser.getUsername())
                    .ifPresentOrElse(
                            oracleUser -> {
                                oracleUser.setPassword(postgresqlUser.getPassword());
                                oracleUser.setRole(postgresqlUser.getRole());
                                oracleUserRepository.save(oracleUser);
                            },
                            () -> {
                                User newOracleUser = new User();
                                newOracleUser.setUsername(postgresqlUser.getUsername());
                                newOracleUser.setPassword(postgresqlUser.getPassword());
                                newOracleUser.setRole(postgresqlUser.getRole());
                                oracleUserRepository.save(newOracleUser);
                            }
                    );
        }
        System.out.println("Users synchronized from PostgreSQL to MySQL and Oracle");
    }

    @Transactional
    public void syncAllDatabases() {
        syncUsersToOracleAndPostgreSQL();
        syncUsersToMySQLAndPostgreSQL();
        syncUsersFromPostgreSQLToOthers();
    }

    @Scheduled(fixedDelay = 300000)
    public void scheduledSync() {
        try {
            syncAllDatabases();
            System.out.println("Scheduled synchronization completed for all databases");
        } catch (Exception e) {
            System.err.println("Database synchronization failed: " + e.getMessage());
        }
    }

    @Transactional
    public void syncToOracle() {
        List<User> mysqlUsers = mysqlUserRepository.findAll();

        for (User mysqlUser : mysqlUsers) {
            oracleUserRepository.findByUsername(mysqlUser.getUsername())
                    .ifPresentOrElse(
                            oracleUser -> {
                                oracleUser.setPassword(mysqlUser.getPassword());
                                oracleUser.setRole(mysqlUser.getRole());
                                oracleUserRepository.save(oracleUser);
                            },
                            () -> {
                                User newOracleUser = new User();
                                newOracleUser.setUsername(mysqlUser.getUsername());
                                newOracleUser.setPassword(mysqlUser.getPassword());
                                newOracleUser.setRole(mysqlUser.getRole());
                                oracleUserRepository.save(newOracleUser);
                            }
                    );
        }
    }

    @Transactional
    public void syncToMySQL() {
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
    }

    @Transactional
    public void syncToPostgreSQL() {
        List<User> mysqlUsers = mysqlUserRepository.findAll();

        for (User mysqlUser : mysqlUsers) {
            postgreSQLUserRepository.findByUsername(mysqlUser.getUsername())
                    .ifPresentOrElse(
                            postgresqlUser -> {
                                postgresqlUser.setPassword(mysqlUser.getPassword());
                                postgresqlUser.setRole(mysqlUser.getRole());
                                postgreSQLUserRepository.save(postgresqlUser);
                            },
                            () -> {
                                User newPostgreSQLUser = new User();
                                newPostgreSQLUser.setUsername(mysqlUser.getUsername());
                                newPostgreSQLUser.setPassword(mysqlUser.getPassword());
                                newPostgreSQLUser.setRole(mysqlUser.getRole());
                                postgreSQLUserRepository.save(newPostgreSQLUser);
                            }
                    );
        }
    }
}