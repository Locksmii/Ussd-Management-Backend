// File: AuthService.java
package springboot_login_page.login_page.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot_login_page.login_page.DTO.AdminRegisterRequest;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.mysql.MySQLUserRepository;
import springboot_login_page.login_page.Repository.oracle.OracleUserRepository;
import springboot_login_page.login_page.Repository.postgresql.PostgreSQLUserRepository;

import java.util.List;

@Service
public class AuthService {

    private final MySQLUserRepository mysqlRepo;
    private final OracleUserRepository oracleRepo;
    private final PostgreSQLUserRepository postgresqlRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(MySQLUserRepository mysqlRepo,
                       OracleUserRepository oracleRepo,
                       PostgreSQLUserRepository postgresqlRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       AuditService auditService) {
        this.mysqlRepo = mysqlRepo;
        this.oracleRepo = oracleRepo;
        this.postgresqlRepo = postgresqlRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    // REMOVED: public register() method - only admin can register users now

    @Transactional
    public void registerUser(AdminRegisterRequest request) {
        // Get current authenticated admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();

        // Validate username format - must be exactly 6 digits
        if (!request.getUsername().matches("^\\d{6}$")) {
            throw new RuntimeException("Username must be exactly 6 digits");
        }

        // Check if username already exists in MySQL
        if (mysqlRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Validate password (optional - add your own validation rules)
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Create user for MySQL
        User mysqlUser = new User();
        mysqlUser.setUsername(request.getUsername());
        mysqlUser.setPassword(encoder.encode(request.getPassword()));
        mysqlUser.setRole(request.getRole() != null ? request.getRole() : User.Role.USER);
        mysqlRepo.save(mysqlUser);

        // Create user for Oracle
        User oracleUser = new User();
        oracleUser.setUsername(request.getUsername());
        oracleUser.setPassword(encoder.encode(request.getPassword()));
        oracleUser.setRole(request.getRole() != null ? request.getRole() : User.Role.USER);
        oracleRepo.save(oracleUser);

        // Create user for PostgreSQL
        User postgresqlUser = new User();
        postgresqlUser.setUsername(request.getUsername());
        postgresqlUser.setPassword(encoder.encode(request.getPassword()));
        postgresqlUser.setRole(request.getRole() != null ? request.getRole() : User.Role.USER);
        postgresqlRepo.save(postgresqlUser);

        // AUDIT LOGGING: Log user creation by admin
        try {
            auditService.logCreate("USER", mysqlUser.getId().toString(), mysqlUser, adminUsername);
            log.info("Admin {} created new user {} with role {}",
                    adminUsername, request.getUsername(), request.getRole());
        } catch (Exception e) {
            log.error("Failed to create audit log for user registration: {}", e.getMessage());
        }
    }

    public String login(String username, String password) {
        // Try MySQL first, fall back to Oracle or PostgreSQL if needed
        User user = mysqlRepo.findByUsername(username)
                .orElseGet(() -> oracleRepo.findByUsername(username)
                        .orElseGet(() -> postgresqlRepo.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"))));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(username);
    }

    public List<User> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admins can view all users");
        }

        return mysqlRepo.findAll();
    }

    @Transactional
    public void updateUserRole(Long userId, User.Role newRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admin can update user roles");
        }

        String adminUsername = auth.getName();
        User mysqlUser = mysqlRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store the user state before update for audit logging
        User beforeUpdate = new User();
        beforeUpdate.setUsername(mysqlUser.getUsername());
        beforeUpdate.setRole(mysqlUser.getRole());

        // Update role in MySQL
        mysqlUser.setRole(newRole);
        mysqlRepo.save(mysqlUser);

        // Update role in Oracle
        oracleRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(oracleUser -> {
                    oracleUser.setRole(newRole);
                    oracleRepo.save(oracleUser);
                });

        // Update role in PostgreSQL
        postgresqlRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(postgresqlUser -> {
                    postgresqlUser.setRole(newRole);
                    postgresqlRepo.save(postgresqlUser);
                });

        // AUDIT LOGGING: Log role update
        try {
            auditService.logUpdate("USER", userId.toString(), beforeUpdate, mysqlUser, adminUsername);
            log.info("Admin {} updated role for user {} to {}",
                    adminUsername, mysqlUser.getUsername(), newRole);
        } catch (Exception e) {
            log.error("Failed to create audit log for role update: {}", e.getMessage());
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admin can delete users");
        }

        String adminUsername = auth.getName();
        User mysqlUser = mysqlRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store user data for audit before deletion
        User userToDelete = new User();
        userToDelete.setId(mysqlUser.getId());
        userToDelete.setUsername(mysqlUser.getUsername());
        userToDelete.setRole(mysqlUser.getRole());

        // Delete from MySQL
        mysqlRepo.delete(mysqlUser);

        // Delete from Oracle
        oracleRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(oracleRepo::delete);

        // Delete from PostgreSQL
        postgresqlRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(postgresqlRepo::delete);

        // AUDIT LOGGING: Log user deletion
        try {
            auditService.logDelete("USER", userId.toString(), userToDelete, adminUsername);
            log.info("Admin {} deleted user {}", adminUsername, mysqlUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to create audit log for user deletion: {}", e.getMessage());
        }
    }

    // Helper method to get current username
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);
}