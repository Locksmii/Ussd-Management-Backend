// File: OracleUSSDCodeRepository.java
package springboot_login_page.login_page.Repository.oracle;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot_login_page.login_page.Entity.USSDCode;
import java.util.Optional;

public interface OracleUSSDCodeRepository extends JpaRepository<USSDCode, Long> {
    Optional<USSDCode> findByCode(String code);
}