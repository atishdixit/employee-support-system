package com.ext.emp.support.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Hardcoded demo accounts - no user database. One login per seeded employee (see
 * EmployeeDirectoryService), username = lowercase employee id, all sharing one demo password.
 *
 * DEMO ONLY: real applications must never hardcode credentials, even hashed ones, in source
 * control. Passwords are still stored BCrypt-hashed (not compared as plain strings) so the
 * verification code path matches what a real user store would look like.
 */
@Service
public class DemoUserService {

    /** Shared demo password for every seeded account - see docs/SETUP.md. */
    public static final String DEMO_PASSWORD = "Passw0rd!";

    public record DemoUser(String username, String passwordHash, String employeeId) {
    }

    private final Map<String, DemoUser> usersByUsername;

    public DemoUserService(PasswordEncoder passwordEncoder) {
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        List<DemoUser> seed = List.of(
                new DemoUser("e001", hash, "E001"),
                new DemoUser("e002", hash, "E002"),
                new DemoUser("e003", hash, "E003"),
                new DemoUser("e004", hash, "E004"),
                new DemoUser("e005", hash, "E005"));
        this.usersByUsername = seed.stream().collect(Collectors.toMap(DemoUser::username, Function.identity()));
    }

    public Optional<DemoUser> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username == null ? null : username.toLowerCase()));
    }
}
