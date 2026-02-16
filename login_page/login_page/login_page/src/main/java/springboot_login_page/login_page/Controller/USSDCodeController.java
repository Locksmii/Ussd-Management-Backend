package springboot_login_page.login_page.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springboot_login_page.login_page.Entity.USSDCode;
import springboot_login_page.login_page.Service.USSDCodeService;

import java.util.List;

@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
public class USSDCodeController {

    private final USSDCodeService ussdCodeService;

    // Get all USSD codes - accessible by both USER and ADMIN
    @GetMapping("/codes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<USSDCode>> getAllUSSDCodes() {
        return ResponseEntity.ok(ussdCodeService.getAllUSSDCodes());
    }

    // Get USSD code by ID - accessible by both USER and ADMIN
    @GetMapping("/codes/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<USSDCode> getUSSDCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(ussdCodeService.getUSSDCodeById(id));
    }

    // Create new USSD code - accessible by both USER and ADMIN
    @PostMapping("/codes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<USSDCode> createUSSDCode(@RequestBody USSDCode code) {
        return ResponseEntity.ok(ussdCodeService.createUSSDCode(code));
    }

    // Update USSD code - accessible by both USER and ADMIN
    @PutMapping("/codes/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> updateUSSDCode(
            @PathVariable Long id,
            @RequestBody USSDCode code) {
        ussdCodeService.updateUSSDCode(id, code);
        return ResponseEntity.ok("USSD code updated successfully");
    }

    // Delete USSD code - accessible by both USER and ADMIN
    @DeleteMapping("/codes/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> deleteUSSDCode(@PathVariable Long id) {
        ussdCodeService.deleteUSSDCode(id);
        return ResponseEntity.ok("USSD code deleted successfully");
    }
}