package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.PlaybookRequest;
import com.example.edupath.demo.dto.PlaybookResponse;

import java.util.List;

public interface PlaybookService {

    List<PlaybookResponse> getAllPlaybooks(String category, String status, String search);

    PlaybookResponse getPlaybookById(Long id);

    PlaybookResponse createPlaybook(String userEmail, PlaybookRequest request);

    PlaybookResponse updatePlaybook(Long id, String userEmail, boolean isAdmin, PlaybookRequest request);

    void deletePlaybook(Long id, String userEmail, boolean isAdmin);
}
