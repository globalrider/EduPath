package com.edupath.qa.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotBlank(message = "Add a title for your question.")
        @Size(max = 200, message = "The title must be 200 characters or fewer.")
        String title,

        @NotBlank(message = "Write your question before submitting.")
        @Size(max = 10000, message = "The question must be 10,000 characters or fewer.")
        String questionBody,

        @NotNull(message = "Choose a question category.")
        QuestionCategory category
) {
}
