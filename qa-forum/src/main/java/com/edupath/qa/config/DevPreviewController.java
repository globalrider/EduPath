package com.edupath.qa.config;

import com.edupath.qa.user.PortalUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/preview-role")
@Profile("dev")
public class DevPreviewController {

    private final JdbcTemplate jdbcTemplate;

    public DevPreviewController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public PreviewStatus previewStatus() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof PortalUserDetails user)
                || !user.email().startsWith("local-preview-")) {
            return new PreviewStatus(true, null);
        }
        Long savedUserId = findUserId(user.email());
        if (savedUserId == null || savedUserId.longValue() != user.id()) {
            return new PreviewStatus(true, null);
        }
        return new PreviewStatus(true, user.role());
    }

    @PostMapping
    public PreviewUser choosePreviewRole(
            @Valid @RequestBody DevPreviewRoleRequest request,
            HttpServletRequest servletRequest
    ) {
        String role = request.role().toUpperCase(Locale.ROOT);
        String email = "local-preview-" + role.toLowerCase(Locale.ROOT) + "@edupath.invalid";
        String name = "Local " + (role.equals("JUNIOR") ? "Junior" : "Senior");

        Long userId = findUserId(email);
        if (userId == null) {
            jdbcTemplate.update(
                    """
                    INSERT INTO users (reg_no, name, email, hashed_password, role)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    null,
                    name,
                    email,
                    "{disabled-preview-account}",
                    role
            );
            userId = findUserId(email);
        }
        if (userId == null) {
            throw new IllegalStateException("The local preview user could not be loaded.");
        }

        PortalUserDetails user = new PortalUserDetails(userId, name, email, "", role);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return new PreviewUser(userId, name, role);
    }

    private Long findUserId(String email) {
        return jdbcTemplate.query(
                        "SELECT user_id FROM users WHERE email = ?",
                        (result, rowNumber) -> result.getLong("user_id"),
                        email
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    public record PreviewStatus(boolean available, String role) {
    }

    public record PreviewUser(long id, String name, String role) {
    }
}
