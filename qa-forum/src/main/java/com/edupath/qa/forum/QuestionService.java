package com.edupath.qa.forum;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional
    public QuestionResponse createQuestion(long juniorId, CreateQuestionRequest request) {
        return questionRepository.create(juniorId, request);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listJuniorQuestions(long juniorId) {
        return questionRepository.findForJunior(juniorId);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listOpenQuestions() {
        return questionRepository.findOpenQuestions();
    }

    @Transactional
    public void answerQuestion(long questionId, long seniorId, CreateAnswerRequest request) {
        if (!questionRepository.existsById(questionId)) {
            throw new QuestionNotFoundException(questionId);
        }
        if (answerRepository.existsForQuestion(questionId)) {
            throw new QuestionAlreadyAnsweredException(questionId, null);
        }
        answerRepository.create(questionId, seniorId, request.answerBody());
    }
}
