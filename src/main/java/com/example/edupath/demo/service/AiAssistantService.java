package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.AiPathRequest;
import com.example.edupath.demo.dto.AiPathResponse;

public interface AiAssistantService {
    AiPathResponse generatePath(AiPathRequest request);
}
