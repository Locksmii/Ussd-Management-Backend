package springboot_login_page.login_page.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import springboot_login_page.login_page.Entity.User;
import springboot_login_page.login_page.Repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(){
        return args -> {

            // Check if admin user exists
            if (userRepository.findByUsername("123456").isEmpty()){
                User admin = new User();
                admin.setUsername("123456");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created: 123456/admin123");
            }

            if (userRepository.findByUsername("654321").isEmpty()){
                User testUser = new User();
                testUser.setUsername("654321");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(User.Role.USER);
                userRepository.save(testUser);
                System.out.println("Test user created: 654321/password123");
            }

        };
    }
}