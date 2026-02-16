package springboot_login_page.login_page.DTO;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}