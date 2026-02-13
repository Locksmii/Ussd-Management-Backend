package springboot_login_page.login_page.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ussd codes")
@Data
public class USSDCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //
    private Long id;

    @Column(nullable = false,unique = true)
    private String code;

    private String description;

    private boolean active;
}
