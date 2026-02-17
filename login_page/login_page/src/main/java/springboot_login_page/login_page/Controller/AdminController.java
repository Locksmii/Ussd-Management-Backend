// File: AdminController.java
package springboot_login_page.login_page.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springboot_login_page.login_page.DTO.AdminRegisterRequest;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // All methods require ADMIN role
public class AdminController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AdminRegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully with role: " + request.getRole());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long userId,
            @RequestParam User.Role role) {
        authService.updateUserRole(userId, role);
        return ResponseEntity.ok("User's role updated to: " + role);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}