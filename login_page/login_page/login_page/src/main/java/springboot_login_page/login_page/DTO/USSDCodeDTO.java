package springboot_login_page.login_page.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class USSDCodeDTO {

    @NotBlank(message = "USSD code is required")
    @Pattern(regexp = "^\\*[0-9\\*]+#$", message = "Invalid USSD code format")
    private String code;

    private String description;

    private boolean active;
}