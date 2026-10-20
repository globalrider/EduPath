package com.example.edupath.demo.config;

import com.example.edupath.demo.security.CustomAccessDeniedHandler;
import com.example.edupath.demo.security.JwtAuthenticationEntryPoint;
import com.example.edupath.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            JwtAuthenticationEntryPoint jwtAuthEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication and documentation/health endpoints
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/health", "/h2-console/**").permitAll()
                
                // Read-only public feed and discussions
                .requestMatchers(HttpMethod.GET, "/api/v1/playbooks/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/questions/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/alumni/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/roadmaps/**").permitAll()

                // Playbook publishing permissions: SENIOR, ALUMNI, ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/playbooks/**").hasAnyRole("SENIOR", "ALUMNI", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/playbooks/**").hasAnyRole("SENIOR", "ALUMNI", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/playbooks/**").hasAnyRole("SENIOR", "ALUMNI", "ADMIN")

                // Alumni profiles: ALUMNI, ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/alumni/**").hasAnyRole("ALUMNI", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/alumni/**").hasAnyRole("ALUMNI", "ADMIN")

                // Answer verification badge: SENIOR, ALUMNI, ADMIN
                .requestMatchers(HttpMethod.PATCH, "/api/v1/questions/answers/*/verify").hasAnyRole("SENIOR", "ALUMNI", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/questions/answers/*/verify").hasAnyRole("SENIOR", "ALUMNI", "ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
