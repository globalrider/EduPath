package com.edupath.qa.user;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final AuthenticatedUserService authenticatedUserService;

    public CurrentUserController(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Authentication authentication) {
        PortalUserDetails user = authenticatedUserService.requireForumUser(authentication);
        return new CurrentUserResponse(user.id(), user.name(), user.email(), user.role());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrfToken(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    public record CurrentUserResponse(long id, String name, String email, String role) {
    }
}
