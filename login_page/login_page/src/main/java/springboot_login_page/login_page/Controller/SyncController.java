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
        syncService.syncToOracle();
        return ResponseEntity.ok("Users synchronized to Oracle");
    }

    @PostMapping("/to-mysql")
    public ResponseEntity<String> syncToMySQL() {
        syncService.syncToMySQL();
        return ResponseEntity.ok("Users synchronized to MySQL");
    }

    @PostMapping("/to-postgresql")
    public ResponseEntity<String> syncToPostgreSQL() {
        syncService.syncToPostgreSQL();
        return ResponseEntity.ok("Users synchronized to PostgreSQL");
    }

    @PostMapping("/all")
    public ResponseEntity<String> syncAll() {
        syncService.syncAllDatabases();
        return ResponseEntity.ok("All databases synchronized");
    }

    @PostMapping("/from-postgresql")
    public ResponseEntity<String> syncFromPostgreSQL() {
        syncService.syncUsersFromPostgreSQLToOthers();
        return ResponseEntity.ok("Users synchronized from PostgreSQL to MySQL and Oracle");
    }
}