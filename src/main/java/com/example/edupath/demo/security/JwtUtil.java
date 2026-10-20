package com.example.edupath.demo.security;

import com.example.edupath.demo.model.User;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtUtil {

    private static final String SECRET = "EduPathSecretKeyForJwtAuthenticationAndRoleAuthorization2026SecureKey!";
    private static final long EXPIRATION_MILLIS = 1000L * 60 * 60 * 24 * 7; // 7 days

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        long exp = now + EXPIRATION_MILLIS;

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format(
            "{\"sub\":\"%s\",\"userId\":%d,\"name\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
            escapeJson(user.getEmail()),
            user.getId(),
            escapeJson(user.getName()),
            user.getRole().name(),
            now / 1000,
            exp / 1000
        );

        String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedHeader + "." + encodedPayload, SECRET);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String calculatedSig = sign(parts[0] + "." + parts[1], SECRET);
            if (!calculatedSig.equals(parts[2])) {
                return false;
            }

            String payload = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            long exp = extractLong(payload, "exp");
            return (exp * 1000) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractString(getPayload(token), "sub");
    }

    public String getRoleFromToken(String token) {
        return extractString(getPayload(token), "role");
    }

    public Long getUserIdFromToken(String token) {
        return extractLong(getPayload(token), "userId");
    }

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        return new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
    }

    private String sign(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private Long extractLong(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }
}
