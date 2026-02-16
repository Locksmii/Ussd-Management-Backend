// File: MySQLUSSDCodeRepository.java
package springboot_login_page.login_page.Repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot_login_page.login_page.Entity.USSDCode;
import java.util.Optional;

public interface MySQLUSSDCodeRepository extends JpaRepository<USSDCode, Long> {
    Optional<USSDCode> findByCode(String code);
}