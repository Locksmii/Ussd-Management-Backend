// DatabaseInitializer.java (更新版)
package springboot_login_page.login_page.Config;

import lombok.RequiredArgsConstructor;
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

    private final MySQLUserRepository mysqlUserRepository;
    private final OracleUserRepository oracleUserRepository;
    private final PostgreSQLUserRepository postgreSQLUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            try {
                // Initialize MySQL with default users
                initializeMySQLDatabase();

                // Initialize Oracle with default users
                initializeOracleDatabase();

                // Initialize PostgreSQL with default users
                initializePostgreSQLDatabase();

                System.out.println("Database initialization completed successfully");
            } catch (Exception e) {
                System.err.println("Error during database initialization: " + e.getMessage());
                // Don't throw exception - just log it and continue
                // This allows the application to start even if initialization fails
            }
        };
    }

    private void initializeMySQLDatabase() {
        // Check if admin user exists in MySQL
        if (mysqlUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                mysqlUserRepository.save(admin);
                System.out.println("MySQL Admin user created: 123456/admin123");
            } catch (Exception e) {
                System.err.println("Failed to create MySQL admin user: " + e.getMessage());
            }
        } else {
            System.out.println("MySQL Admin user already exists");
        }

        // Check if test user exists in MySQL
        if (mysqlUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                mysqlUserRepository.save(testUser);
                System.out.println("MySQL Test user created: 654321/password123");
            } catch (Exception e) {
                System.err.println("Failed to create MySQL test user: " + e.getMessage());
            }
        } else {
            System.out.println("MySQL Test user already exists");
        }
    }

    private void initializeOracleDatabase() {
        // Check if admin user exists in Oracle
        if (oracleUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                oracleUserRepository.save(admin);
                System.out.println("Oracle Admin user created: 123456/admin123");
            } catch (Exception e) {
                System.err.println("Failed to create Oracle admin user: " + e.getMessage());
            }
        } else {
            System.out.println("Oracle Admin user already exists");
        }

        // Check if test user exists in Oracle
        if (oracleUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                oracleUserRepository.save(testUser);
                System.out.println("Oracle Test user created: 654321/password123");
            } catch (Exception e) {
                System.err.println("Failed to create Oracle test user: " + e.getMessage());
            }
        } else {
            System.out.println("Oracle Test user already exists");
        }
    }

    private void initializePostgreSQLDatabase() {
        // Check if admin user exists in PostgreSQL
        if (postgreSQLUserRepository.findByUsername("123456").isEmpty()) {
            try {
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                postgreSQLUserRepository.save(admin);
                System.out.println("PostgreSQL Admin user created: 123456/admin123");
            } catch (Exception e) {
                System.err.println("Failed to create PostgreSQL admin user: " + e.getMessage());
            }
        } else {
            System.out.println("PostgreSQL Admin user already exists");
        }

        // Check if test user exists in PostgreSQL
        if (postgreSQLUserRepository.findByUsername("654321").isEmpty()) {
            try {
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                postgreSQLUserRepository.save(testUser);
                System.out.println("PostgreSQL Test user created: 654321/password123");
            } catch (Exception e) {
                System.err.println("Failed to create PostgreSQL test user: " + e.getMessage());
            }
        } else {
            System.out.println("PostgreSQL Test user already exists");
        }
    }
}