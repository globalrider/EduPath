package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AlumniProfileDto;
import com.example.edupath.demo.dto.AuthRequest;
import com.example.edupath.demo.dto.AuthResponse;
import com.example.edupath.demo.dto.UserProfileDto;
import com.example.edupath.demo.exception.ResourceNotFoundException;
import com.example.edupath.demo.model.AlumniProfile;
import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;
import com.example.edupath.demo.repository.AlumniProfileRepository;
import com.example.edupath.demo.repository.UserRepository;
import com.example.edupath.demo.security.JwtUtil;
import com.example.edupath.demo.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
            UserRepository userRepository,
            AlumniProfileRepository alumniProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.alumniProfileRepository = alumniProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }
        if (request.getRegNo() != null && !request.getRegNo().isBlank()) {
            if (userRepository.existsByRegNo(request.getRegNo())) {
                throw new IllegalArgumentException("Registration number '" + request.getRegNo() + "' is already registered");
            }
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.JUNIOR;

        User user = new User();
        user.setRegNo(request.getRegNo());
        user.setName(request.getName() != null ? request.getName() : "Student");
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(assignedRole);

        User savedUser = userRepository.save(user);

        // If registered as ALUMNI, initialize alumni_profiles row
        if (assignedRole == Role.ALUMNI) {
            AlumniProfile alumniProfile = new AlumniProfile(savedUser, null, null);
            alumniProfileRepository.save(alumniProfile);
        }

        String token = jwtUtil.generateToken(savedUser);
        return AuthResponse.fromUser(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials: user not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.fromUser(user, token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(String email) {
        User user = getCurrentUser(email);
        AlumniProfileDto alumniDto = null;

        if (user.getRole() == Role.ALUMNI) {
            alumniDto = alumniProfileRepository.findById(user.getId())
                    .map(AlumniProfileDto::fromEntity)
                    .orElse(null);
        }

        return new UserProfileDto(
            user.getId(),
            user.getRegNo(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            alumniDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }
}
