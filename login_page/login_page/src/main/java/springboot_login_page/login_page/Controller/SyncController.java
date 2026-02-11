package springboot_login_page.login_page.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springboot_login_page.login_page.Service.DatabaseSyncService;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SyncController {

    private final DatabaseSyncService syncService;

    @PostMapping("/to-oracle")
    public ResponseEntity<String> syncToOracle() {
        syncService.syncUsersToOracle();
        return ResponseEntity.ok("Users synchronized to Oracle");
    }

    @PostMapping("/to-mysql")
    public ResponseEntity<String> syncToMySQL() {
        syncService.syncUsersToMySQL();
        return ResponseEntity.ok("Users synchronized to MySQL");
    }

    @PostMapping("/both")
    public ResponseEntity<String> syncBoth() {
        syncService.syncBothDatabases();
        return ResponseEntity.ok("Databases synchronized bi-directionally");
    }
}