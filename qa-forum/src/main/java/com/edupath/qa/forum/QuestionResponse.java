package com.edupath.qa.forum;

import java.time.Instant;

public record QuestionResponse(
        long id,
        String title,
        String body,
        String category,
        QuestionStatus status,
        long authorId,
        String authorName,
        Instant createdAt,
        AnswerResponse answer
) {
}
