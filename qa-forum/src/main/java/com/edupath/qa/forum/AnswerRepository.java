package com.edupath.qa.forum;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnswerRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnswerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(long questionId, long seniorId, String answerBody) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO answers (question_id, author_id, answer_body)
                    VALUES (?, ?, ?)
                    """,
                    questionId,
                    seniorId,
                    answerBody.trim()
            );
        } catch (DuplicateKeyException exception) {
            throw new QuestionAlreadyAnsweredException(questionId, exception);
        }
    }

    public boolean existsForQuestion(long questionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM answers WHERE question_id = ?",
                Integer.class,
                questionId
        );
        return count != null && count > 0;
    }
}
