package springboot_login_page.login_page.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import springboot_login_page.login_page.DTO.AdminRegisterRequest;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.UserRepository;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repo,
                       PasswordEncoder encoder,
                       JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public void register(String username, String password) {
        // Validate username format - must be exactly 6 digits
        if (!username.matches("^\\d{6}$")) {
            throw new RuntimeException("Username must be exactly 6 digits");
        }

        // Check if username already exists
        if (repo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole(User.Role.USER);
        repo.save(user);
    }

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

        // Check if username already exists
        if (repo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        repo.save(user);
    }

    public String login(String username, String password) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        return repo.findAll();
    }

    public void updateUserRole(Long userId, User.Role newRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admin can update user roles");
        }

        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        repo.save(user);
    }

    public void deleteUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only admin can delete users");
        }

        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        repo.delete(user);
    }
}