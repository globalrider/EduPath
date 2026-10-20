package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.AnswerRequest;
import com.example.edupath.demo.dto.AnswerResponse;
import com.example.edupath.demo.dto.QuestionRequest;
import com.example.edupath.demo.dto.QuestionResponse;

import java.util.List;

public interface QuestionService {

    List<QuestionResponse> getAllQuestions(String category, String search);

    QuestionResponse getQuestionById(Long id);

    QuestionResponse createQuestion(String userEmail, QuestionRequest request);

    void deleteQuestion(Long id, String userEmail, boolean isAdmin);

    AnswerResponse addAnswer(Long questionId, String userEmail, AnswerRequest request);

    AnswerResponse verifyAnswer(Long answerId, String userEmail, boolean isAdmin);

    void deleteAnswer(Long answerId, String userEmail, boolean isAdmin);
}
