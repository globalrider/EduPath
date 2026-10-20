package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.AuthRequest;
import com.example.edupath.demo.dto.AuthResponse;
import com.example.edupath.demo.dto.UserProfileDto;
import com.example.edupath.demo.model.User;

public interface AuthService {

    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    UserProfileDto getCurrentUserProfile(String email);

    User getCurrentUser(String email);
}
