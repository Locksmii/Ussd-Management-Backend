package springboot_login_page.login_page.Config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.mysql.MySQLUserRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUserRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLUserRepository;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final MySQLUserRepository mysqlUserRepository;
    private final OracleUserRepository oracleUserRepository;
    private final PostgreSQLUserRepository postgreSQLUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            try {
                initializeMySQLDatabase();
                initializeOracleDatabase();
                initializePostgreSQLDatabase();
                log.info("Database initialization completed successfully");
            } catch (Exception e) {
                log.error("Error during database initialization: {}", e.getMessage());
            }
        };
    }

    private void initializeMySQLDatabase() {
        if (mysqlUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                mysqlUserRepository.save(admin);
                log.info("MySQL Admin user created: 123456");
            } catch (Exception e) {
                log.error("Failed to create MySQL admin user: {}", e.getMessage());
            }
        } else {
            log.info("MySQL Admin user already exists");
        }

        if (mysqlUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                mysqlUserRepository.save(testUser);
                log.info("MySQL Test user created: 654321");
            } catch (Exception e) {
                log.error("Failed to create MySQL test user: {}", e.getMessage());
            }
        } else {
            log.info("MySQL Test user already exists");
        }
    }

    private void initializeOracleDatabase() {
        if (oracleUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                oracleUserRepository.save(admin);
                log.info("Oracle Admin user created: 123456");
            } catch (Exception e) {
                log.error("Failed to create Oracle admin user: {}", e.getMessage());
            }
        } else {
            log.info("Oracle Admin user already exists");
        }

        if (oracleUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                oracleUserRepository.save(testUser);
                log.info("Oracle Test user created: 654321");
            } catch (Exception e) {
                log.error("Failed to create Oracle test user: {}", e.getMessage());
            }
        } else {
            log.info("Oracle Test user already exists");
        }
    }

    private void initializePostgreSQLDatabase() {
        if (postgreSQLUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                postgreSQLUserRepository.save(admin);
                log.info("PostgreSQL Admin user created: 123456");
            } catch (Exception e) {
                log.error("Failed to create PostgreSQL admin user: {}", e.getMessage());
            }
        } else {
            log.info("PostgreSQL Admin user already exists");
        }

        if (postgreSQLUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                postgreSQLUserRepository.save(testUser);
                log.info("PostgreSQL Test user created: 654321");
            } catch (Exception e) {
                log.error("Failed to create PostgreSQL test user: {}", e.getMessage());
            }
        } else {
            log.info("PostgreSQL Test user already exists");
        }
    }
}