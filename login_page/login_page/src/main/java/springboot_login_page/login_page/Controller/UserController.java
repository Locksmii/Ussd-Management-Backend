// File: UserController.java
package springboot_login_page.login_page.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.mysql.MySQLUserRepository;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final MySQLUserRepository mySQLUserRepository;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = mySQLUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updateUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = mySQLUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only allowed fields
        if (updateUser.getUsername() != null && !updateUser.getUsername().isEmpty()) {
            // Validate username format (6 digits)
            if (!updateUser.getUsername().matches("^\\d{6}$")) {
                throw new RuntimeException("Username must be exactly 6 digits");
            }

            // Check if new username is already taken
            if (!username.equals(updateUser.getUsername()) &&
                    mySQLUserRepository.findByUsername(updateUser.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }

            user.setUsername(updateUser.getUsername());
        }

        // Note: Password updates should be handled through a separate endpoint with proper validation
        // Role cannot be updated by user (admin only)

        mySQLUserRepository.save(user);
        return ResponseEntity.ok(user);
    }
}