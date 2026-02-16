// File: AuthService.java (Complete Updated Version with Audit Logging)
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
    private final AuditService auditService; // Added AuditService

    public AuthService(MySQLUserRepository mysqlRepo,
                       OracleUserRepository oracleRepo,
                       PostgreSQLUserRepository postgresqlRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       AuditService auditService) { // Updated constructor
        this.mysqlRepo = mysqlRepo;
        this.oracleRepo = oracleRepo;
        this.postgresqlRepo = postgresqlRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public void register(String username, String password) {
        // Validate username format - must be exactly 6 digits
        if (!username.matches("^\\d{6}$")) {
            throw new RuntimeException("Username must be exactly 6 digits");
        }

        // Check if username already exists in MySQL
        if (mysqlRepo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create user for MySQL
        User mysqlUser = new User();
        mysqlUser.setUsername(username);
        mysqlUser.setPassword(encoder.encode(password));
        mysqlUser.setRole(User.Role.USER);
        mysqlRepo.save(mysqlUser);

        // Create user for Oracle
        User oracleUser = new User();
        oracleUser.setUsername(username);
        oracleUser.setPassword(encoder.encode(password));
        oracleUser.setRole(User.Role.USER);
        oracleRepo.save(oracleUser);

        // Create user for PostgreSQL
        User postgresqlUser = new User();
        postgresqlUser.setUsername(username);
        postgresqlUser.setPassword(encoder.encode(password));
        postgresqlUser.setRole(User.Role.USER);
        postgresqlRepo.save(postgresqlUser);

        // AUDIT LOGGING: Log user creation
        try {
            auditService.logCreate("USER", mysqlUser.getId().toString(), mysqlUser, username);
        } catch (Exception e) {
            // Log error but don't fail the registration
            System.err.println("Failed to create audit log for user registration: " + e.getMessage());
        }
    }

    @Transactional
    public void adminRegister(AdminRegisterRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admin can register users with roles");
        }

        // Validate username format - must be exactly 6 digits
        if (!request.getUsername().matches("^\\d{6}$")) {
            throw new RuntimeException("Username must be exactly 6 digits");
        }

        // Check if username already exists in MySQL
        if (mysqlRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create user for MySQL
        User mysqlUser = new User();
        mysqlUser.setUsername(request.getUsername());
        mysqlUser.setPassword(encoder.encode(request.getPassword()));
        mysqlUser.setRole(request.getRole());
        mysqlRepo.save(mysqlUser);

        // Create user for Oracle
        User oracleUser = new User();
        oracleUser.setUsername(request.getUsername());
        oracleUser.setPassword(encoder.encode(request.getPassword()));
        oracleUser.setRole(request.getRole());
        oracleRepo.save(oracleUser);

        // Create user for PostgreSQL
        User postgresqlUser = new User();
        postgresqlUser.setUsername(request.getUsername());
        postgresqlUser.setPassword(encoder.encode(request.getPassword()));
        postgresqlUser.setRole(request.getRole());
        postgresqlRepo.save(postgresqlUser);

        // AUDIT LOGGING: Log admin user creation
        try {
            String adminUsername = auth.getName();
            auditService.logCreate("USER", mysqlUser.getId().toString(), mysqlUser, adminUsername);
        } catch (Exception e) {
            System.err.println("Failed to create audit log for admin registration: " + e.getMessage());
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

        // AUDIT LOGGING: Could log successful login if needed
        // Note: Login is a read operation, so audit logging is optional
        // Uncomment if you want to track logins
        /*
        try {
            auditService.logCreate("LOGIN", username, "User logged in", username);
        } catch (Exception e) {
            System.err.println("Failed to create audit log for login: " + e.getMessage());
        }
        */

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

        // AUDIT LOGGING: Could log user list access if needed
        // Uncomment if you want to track who accessed user list
        /*
        try {
            auditService.logCreate("USER_LIST", "ALL", "Admin accessed user list", auth.getName());
        } catch (Exception e) {
            System.err.println("Failed to create audit log for user list access: " + e.getMessage());
        }
        */

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
            String adminUsername = auth.getName();
            auditService.logUpdate("USER", userId.toString(), beforeUpdate, mysqlUser, adminUsername);
        } catch (Exception e) {
            System.err.println("Failed to create audit log for role update: " + e.getMessage());
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
            String adminUsername = auth.getName();
            auditService.logDelete("USER", userId.toString(), userToDelete, adminUsername);
        } catch (Exception e) {
            System.err.println("Failed to create audit log for user deletion: " + e.getMessage());
        }
    }

    // Helper method to get current username (if needed elsewhere)
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}