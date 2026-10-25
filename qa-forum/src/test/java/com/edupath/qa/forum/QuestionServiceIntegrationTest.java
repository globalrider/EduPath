package com.edupath.qa.forum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class QuestionServiceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    private long juniorId;
    private long otherJuniorId;
    private long seniorId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM answers");
        jdbcTemplate.update("DELETE FROM questions");
        jdbcTemplate.update("DELETE FROM users");
        juniorId = createUser("junior@example.test", "Junior One", "JUNIOR");
        otherJuniorId = createUser("other@example.test", "Junior Two", "JUNIOR");
        seniorId = createUser("senior@example.test", "Senior One", "SENIOR");
    }

    @Test
    void juniorQuestionIsVisibleToSeniorAndOnlyItsOwnerSeesItsAnswer() {
        QuestionResponse question = createQuestion(juniorId, "Choosing a first internship");

        List<QuestionResponse> openQuestions = questionService.listOpenQuestions();
        assertThat(openQuestions).extracting(QuestionResponse::id).contains(question.id());

        questionService.answerQuestion(
                question.id(),
                seniorId,
                new CreateAnswerRequest("Start with projects that match your interests.")
        );

        QuestionResponse saved = questionRepository.findForJunior(question.id(), juniorId).orElseThrow();
        assertThat(saved.status()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(saved.answer()).isNotNull();
        assertThat(saved.answer().body()).isEqualTo("Start with projects that match your interests.");
        assertThat(saved.answer().authorName()).isEqualTo("Senior One");
        assertThat(questionRepository.findForJunior(question.id(), otherJuniorId)).isEmpty();
        assertThat(questionService.listOpenQuestions()).doesNotContain(saved);
    }

    @Test
    void aCompletedQuestionCannotReceiveASecondAnswer() {
        QuestionResponse question = createQuestion(juniorId, "How should I prepare?");
        questionService.answerQuestion(question.id(), seniorId, new CreateAnswerRequest("Build a weekly plan."));

        assertThatThrownBy(() -> questionService.answerQuestion(
                question.id(),
                seniorId,
                new CreateAnswerRequest("A second answer.")
        )).isInstanceOf(QuestionAlreadyAnsweredException.class);
    }

    @Test
    void missingQuestionCannotBeAnswered() {
        assertThatThrownBy(() -> questionService.answerQuestion(
                Long.MAX_VALUE,
                seniorId,
                new CreateAnswerRequest("An answer.")
        )).isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void concurrentSeniorAnswersAcceptExactlyOneWinner() throws Exception {
        QuestionResponse question = createQuestion(juniorId, "Which skills should I focus on?");
        int contenders = 2;
        CountDownLatch ready = new CountDownLatch(contenders);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(contenders)) {
            Future<?> first = executor.submit(() -> attemptAnswer(
                    question.id(), seniorId, ready, start, accepted, rejected, "First response."
            ));
            Future<?> second = executor.submit(() -> attemptAnswer(
                    question.id(), seniorId, ready, start, accepted, rejected, "Second response."
            ));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertThat(accepted.get()).isEqualTo(1);
        assertThat(rejected.get()).isEqualTo(1);
        assertThat(questionRepository.findForJunior(question.id(), juniorId).orElseThrow().status())
                .isEqualTo(QuestionStatus.ANSWERED);
    }

    private void attemptAnswer(
            long questionId,
            long answererId,
            CountDownLatch ready,
            CountDownLatch start,
            AtomicInteger accepted,
            AtomicInteger rejected,
            String body
    ) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The concurrent answer test did not start.");
            }
            questionService.answerQuestion(questionId, answererId, new CreateAnswerRequest(body));
            accepted.incrementAndGet();
        } catch (QuestionAlreadyAnsweredException exception) {
            rejected.incrementAndGet();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The concurrent answer test was interrupted.", exception);
        }
    }

    private QuestionResponse createQuestion(long authorId, String title) {
        return questionService.createQuestion(
                authorId,
                new CreateQuestionRequest(title, "Please share your advice.", QuestionCategory.ACADEMICS)
        );
    }

    private long createUser(String email, String name, String role) {
        jdbcTemplate.update(
                """
                INSERT INTO users (reg_no, name, email, hashed_password, role)
                VALUES (?, ?, ?, ?, ?)
                """,
                null,
                name,
                email,
                "{noop}test-only",
                role
        );
        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE email = ?",
                Long.class,
                email
        );
    }
}
