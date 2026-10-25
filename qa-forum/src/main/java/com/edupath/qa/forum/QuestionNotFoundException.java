package com.edupath.qa.forum;

public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(long questionId) {
        super("Question " + questionId + " was not found.");
    }
}
