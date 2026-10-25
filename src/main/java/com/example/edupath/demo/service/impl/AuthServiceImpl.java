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
        if (request.getRegNo() == null || request.getRegNo().trim().isBlank()) {
            throw new IllegalArgumentException("Register Number is required to uniquely identify your account");
        }
        String cleanRegNo = request.getRegNo().trim().toUpperCase();
        if (userRepository.existsByRegNoIgnoreCase(cleanRegNo)) {
            throw new IllegalArgumentException("Register Number '" + cleanRegNo + "' is already registered in EduPath");
        }

        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new IllegalArgumentException("Email address is required for registration");
        }
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("User with email '" + cleanEmail + "' already exists");
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.JUNIOR;

        User user = new User();
        user.setRegNo(cleanRegNo);
        user.setName(request.getName() != null && !request.getName().trim().isBlank() ? request.getName().trim() : "Student");
        user.setEmail(cleanEmail);
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
        String identifier = null;
        if (request.getRegNo() != null && !request.getRegNo().trim().isBlank()) {
            identifier = request.getRegNo().trim();
        } else if (request.getEmail() != null && !request.getEmail().trim().isBlank()) {
            identifier = request.getEmail().trim();
        }

        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Please provide your Register Number or Email to log in");
        }

        final String query = identifier;
        User user = userRepository.findByRegNoIgnoreCase(query)
                .or(() -> userRepository.findByEmailIgnoreCase(query))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials: no account found with Register Number or Email '" + query + "'"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password for " + query);
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
