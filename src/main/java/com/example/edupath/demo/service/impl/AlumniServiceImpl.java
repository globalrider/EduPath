package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AlumniProfileDto;
import com.example.edupath.demo.exception.ResourceNotFoundException;
import com.example.edupath.demo.model.AlumniProfile;
import com.example.edupath.demo.model.User;
import com.example.edupath.demo.repository.AlumniProfileRepository;
import com.example.edupath.demo.repository.UserRepository;
import com.example.edupath.demo.service.AlumniService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlumniServiceImpl implements AlumniService {

    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;

    public AlumniServiceImpl(AlumniProfileRepository alumniProfileRepository, UserRepository userRepository) {
        this.alumniProfileRepository = alumniProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileDto> getAllAlumni() {
        return alumniProfileRepository.findAll().stream()
                .map(AlumniProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileDto getAlumniById(Long alumniId) {
        AlumniProfile profile = alumniProfileRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni profile not found for user ID: " + alumniId));
        return AlumniProfileDto.fromEntity(profile);
    }

    @Override
    public AlumniProfileDto updateCurrentAlumniProfile(String email, AlumniProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        AlumniProfile profile = alumniProfileRepository.findById(user.getId())
                .orElseGet(() -> new AlumniProfile(user, null, null));

        if (dto.getCurrentCompany() != null) {
            profile.setCurrentCompany(dto.getCurrentCompany());
        }
        if (dto.getJobTitle() != null) {
            profile.setJobTitle(dto.getJobTitle());
        }

        AlumniProfile saved = alumniProfileRepository.save(profile);
        return AlumniProfileDto.fromEntity(saved);
    }

    @Override
    public AlumniProfileDto updateAlumniProfileByAdmin(Long alumniId, AlumniProfileDto dto) {
        User user = userRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + alumniId));

        AlumniProfile profile = alumniProfileRepository.findById(alumniId)
                .orElseGet(() -> new AlumniProfile(user, null, null));

        if (dto.getCurrentCompany() != null) {
            profile.setCurrentCompany(dto.getCurrentCompany());
        }
        if (dto.getJobTitle() != null) {
            profile.setJobTitle(dto.getJobTitle());
        }

        AlumniProfile saved = alumniProfileRepository.save(profile);
        return AlumniProfileDto.fromEntity(saved);
    }
}
