// File: USSDCode.java
package springboot_login_page.login_page.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ussd_codes")
@Data
public class USSDCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., *123#

    private String description;

    private boolean active;
}