package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.AlumniProfileDto;

import java.util.List;

public interface AlumniService {

    List<AlumniProfileDto> getAllAlumni();

    AlumniProfileDto getAlumniById(Long alumniId);

    AlumniProfileDto updateCurrentAlumniProfile(String email, AlumniProfileDto dto);

    AlumniProfileDto updateAlumniProfileByAdmin(Long alumniId, AlumniProfileDto dto);
}
