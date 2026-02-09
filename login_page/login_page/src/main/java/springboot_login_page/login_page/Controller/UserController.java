package springboot_login_page.login_page.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.UserRepository;
import springboot_login_page.login_page.Service.JwtService;

import java.util.Optional;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    //Get current users profiles

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return  ResponseEntity.ok(user);
    }

    //Update current user's profile excluding the role
    @PutMapping("/profile")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updateUser){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->new RuntimeException("User not found"));

        //Users can only update certain fields
        user.setUsername(updateUser.getUsername());

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

}