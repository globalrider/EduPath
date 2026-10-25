package com.edupath.qa.forum;

import java.time.Instant;

public record AnswerResponse(
        long id,
        String body,
        long authorId,
        String authorName,
        Instant createdAt,
        boolean verified
) {
}
