package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String regNo;
    private String name;
    private String email;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String regNo, String name, String email, Role role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.regNo = regNo;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static AuthResponse fromUser(User user, String token) {
        return new AuthResponse(
            token,
            user.getId(),
            user.getRegNo(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
