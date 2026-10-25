package com.edupath.qa.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnswerRequest(
        @NotBlank(message = "Write an answer before submitting.")
        @Size(max = 10000, message = "The answer must be 10,000 characters or fewer.")
        String answerBody
) {
}
