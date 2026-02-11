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

import java.util.List;

@Service
public class AuthService {

    private final MySQLUserRepository mysqlRepo;
    private final OracleUserRepository oracleRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(MySQLUserRepository mysqlRepo,
                       OracleUserRepository oracleRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService) {
        this.mysqlRepo = mysqlRepo;
        this.oracleRepo = oracleRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
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
    }

    public String login(String username, String password) {
        // Try MySQL first, fall back to Oracle if needed
        User user = mysqlRepo.findByUsername(username)
                .orElseGet(() -> oracleRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(username);
    }

    public List<User> getAllUsers(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin){
            throw new RuntimeException("Only admins can view all users");
        }

        // Return users from MySQL (or combine both if needed)
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

        // Update in MySQL
        User mysqlUser = mysqlRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        mysqlUser.setRole(newRole);
        mysqlRepo.save(mysqlUser);

        // Update in Oracle
        oracleRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(oracleUser -> {
                    oracleUser.setRole(newRole);
                    oracleRepo.save(oracleUser);
                });
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

        // Delete from MySQL
        User mysqlUser = mysqlRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        mysqlRepo.delete(mysqlUser);

        // Delete from Oracle
        oracleRepo.findByUsername(mysqlUser.getUsername())
                .ifPresent(oracleRepo::delete);
    }
}