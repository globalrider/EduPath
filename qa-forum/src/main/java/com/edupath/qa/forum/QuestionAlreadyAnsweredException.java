package com.edupath.qa.forum;

public class QuestionAlreadyAnsweredException extends RuntimeException {

    public QuestionAlreadyAnsweredException(long questionId, Throwable cause) {
        super("Question " + questionId + " has already been answered.", cause);
    }
}
