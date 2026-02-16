// File: AdminRegisterRequest.java
package springboot_login_page.login_page.DTO;

import lombok.Data;
import springboot_login_page.login_page.Entity.User;

@Data
public class AdminRegisterRequest {
    private String username;
    private String password;
    private User.Role role;
}