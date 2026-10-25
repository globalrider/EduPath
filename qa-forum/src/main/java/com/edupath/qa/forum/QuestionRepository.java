package com.edupath.qa.forum;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class QuestionRepository {

    private static final String QUESTION_COLUMNS = """
            SELECT q.question_id, q.title, q.question_body, q.category, q.author_id,
                   q.created_at, author.name AS author_name,
                   a.answer_id, a.answer_body, a.author_id AS answer_author_id,
                   a.created_at AS answer_created_at, a.is_verified_answer,
                   answer_author.name AS answer_author_name
            FROM questions q
            JOIN users author ON author.user_id = q.author_id
            LEFT JOIN answers a ON a.question_id = q.question_id
            LEFT JOIN users answer_author ON answer_author.user_id = a.author_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public QuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QuestionResponse create(long authorId, CreateQuestionRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO questions (author_id, title, question_body, category)
                    VALUES (?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, authorId);
            statement.setString(2, request.title().trim());
            statement.setString(3, request.questionBody().trim());
            statement.setString(4, request.category().databaseValue());
            return statement;
        }, keyHolder);

        Number key = generatedQuestionId(keyHolder);
        if (key == null) {
            throw new IllegalStateException("The database did not return a question ID.");
        }
        return findForJunior(key.longValue(), authorId)
                .orElseThrow(() -> new IllegalStateException("The saved question could not be loaded."));
    }

    private Number generatedQuestionId(KeyHolder keyHolder) {
        if (keyHolder.getKeyList().isEmpty()) {
            return null;
        }
        return keyHolder.getKeyList().getFirst().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase("question_id"))
                .map(java.util.Map.Entry::getValue)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .orElseGet(() -> {
                    if (keyHolder.getKeyList().getFirst().size() == 1) {
                        Object onlyKey = keyHolder.getKeyList().getFirst().values().iterator().next();
                        return onlyKey instanceof Number number ? number : null;
                    }
                    return null;
                });
    }

    public List<QuestionResponse> findForJunior(long juniorId) {
        return jdbcTemplate.query(
                QUESTION_COLUMNS + " WHERE q.author_id = ? ORDER BY q.created_at DESC, q.question_id DESC",
                (result, rowNumber) -> mapQuestion(result.getLong("question_id"),
                        result.getString("title"),
                        result.getString("question_body"),
                        result.getString("category"),
                        result.getLong("author_id"),
                        result.getString("author_name"),
                        result.getTimestamp("created_at"),
                        result.getObject("answer_id") == null ? null : result.getLong("answer_id"),
                        result.getString("answer_body"),
                        result.getObject("answer_author_id") == null ? null : result.getLong("answer_author_id"),
                        result.getString("answer_author_name"),
                        result.getTimestamp("answer_created_at"),
                        result.getBoolean("is_verified_answer")),
                juniorId
        );
    }

    public Optional<QuestionResponse> findForJunior(long questionId, long juniorId) {
        List<QuestionResponse> questions = jdbcTemplate.query(
                QUESTION_COLUMNS + " WHERE q.question_id = ? AND q.author_id = ?",
                (result, rowNumber) -> mapQuestion(result.getLong("question_id"),
                        result.getString("title"),
                        result.getString("question_body"),
                        result.getString("category"),
                        result.getLong("author_id"),
                        result.getString("author_name"),
                        result.getTimestamp("created_at"),
                        result.getObject("answer_id") == null ? null : result.getLong("answer_id"),
                        result.getString("answer_body"),
                        result.getObject("answer_author_id") == null ? null : result.getLong("answer_author_id"),
                        result.getString("answer_author_name"),
                        result.getTimestamp("answer_created_at"),
                        result.getBoolean("is_verified_answer")),
                questionId,
                juniorId
        );
        return questions.stream().findFirst();
    }

    public List<QuestionResponse> findOpenQuestions() {
        return jdbcTemplate.query(
                QUESTION_COLUMNS
                        + " WHERE a.answer_id IS NULL ORDER BY q.created_at DESC, q.question_id DESC",
                (result, rowNumber) -> mapQuestion(result.getLong("question_id"),
                        result.getString("title"),
                        result.getString("question_body"),
                        result.getString("category"),
                        result.getLong("author_id"),
                        result.getString("author_name"),
                        result.getTimestamp("created_at"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        false)
        );
    }

    public boolean existsById(long questionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM questions WHERE question_id = ?",
                Integer.class,
                questionId
        );
        return count != null && count > 0;
    }

    private QuestionResponse mapQuestion(
            long id,
            String title,
            String body,
            String category,
            long authorId,
            String authorName,
            Timestamp createdAt,
            Long answerId,
            String answerBody,
            Long answerAuthorId,
            String answerAuthorName,
            Timestamp answerCreatedAt,
            boolean verified
    ) {
        AnswerResponse answer = answerId == null
                ? null
                : new AnswerResponse(
                        answerId,
                        answerBody,
                        answerAuthorId,
                        answerAuthorName,
                        toInstant(answerCreatedAt),
                        verified
                );
        return new QuestionResponse(
                id,
                title,
                body,
                category,
                answer == null ? QuestionStatus.OPEN : QuestionStatus.ANSWERED,
                authorId,
                authorName,
                toInstant(createdAt),
                answer
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
