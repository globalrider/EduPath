package com.edupath.qa.user;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticatedUserService {

    private final JdbcTemplate jdbcTemplate;

    public AuthenticatedUserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PortalUserDetails requireForumUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);
        return jdbcTemplate.query(
                        """
                        SELECT user_id, name, email, role
                        FROM users
                        WHERE LOWER(email) = ?
                        """,
                        (result, rowNumber) -> new PortalUserDetails(
                                result.getLong("user_id"),
                                result.getString("name"),
                                result.getString("email"),
                                "",
                                result.getString("role").toUpperCase(Locale.ROOT)
                        ),
                        email
                )
                .stream()
                .filter(user -> user.role().equals("JUNIOR") || user.role().equals("SENIOR"))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only EduPath Junior and Senior accounts can use the Q&A forum."
                ));
    }
}
