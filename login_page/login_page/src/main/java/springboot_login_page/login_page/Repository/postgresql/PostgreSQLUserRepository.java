// PostgreSQLUserRepository.java
package springboot_login_page.login_page.Repository.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot_login_page.login_page.Entity.User;

import java.util.Optional;

public interface PostgreSQLUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}