package springboot_login_page.login_page.Repository.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot_login_page.login_page.Entity.USSDCode;
import java.util.Optional;

public interface PostgreSQLUSSDCodeRepository extends JpaRepository<USSDCode, Long> {
    Optional<USSDCode> findByCode(String code);
}