package com.edupath.qa.forum;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edupath.qa.user.PortalUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionControllerSecurityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    private PortalUserDetails junior;
    private PortalUserDetails otherJunior;
    private PortalUserDetails senior;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM answers");
        jdbcTemplate.update("DELETE FROM questions");
        jdbcTemplate.update("DELETE FROM users");
        junior = createUser("junior@example.test", "Junior One", "JUNIOR");
        otherJunior = createUser("other@example.test", "Junior Two", "JUNIOR");
        senior = createUser("senior@example.test", "Senior One", "SENIOR");
    }

    @Test
    void endpointsRequireTheMatchingAuthenticatedRole() throws Exception {
        mockMvc.perform(get("/api/junior/questions"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/junior/questions").with(user(senior)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/senior/questions").with(user(junior)))
                .andExpect(status().isForbidden());
    }

    @Test
    void juniorCanSubmitAQuestionThatAppearsInTheSeniorQueue() throws Exception {
        mockMvc.perform(post("/api/junior/questions")
                        .with(user(junior))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "How do I start an internship search?",
                                  "questionBody": "I am looking for a first internship. What should I prepare?",
                                  "category": "INTERNSHIPS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId").value(junior.id()))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/senior/questions").with(user(senior)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("How do I start an internship search?"))
                .andExpect(jsonPath("$[0].authorName").value("Junior One"));
    }

    @Test
    void juniorCanOnlyReadTheirOwnQuestion() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO questions (author_id, title, question_body, category)
                VALUES (?, ?, ?, ?)
                """,
                junior.id(),
                "A private question",
                "Question text",
                "Academics"
        );
        Long questionId = jdbcTemplate.queryForObject(
                "SELECT question_id FROM questions WHERE author_id = ?",
                Long.class,
                junior.id()
        );

        mockMvc.perform(get("/api/junior/questions/{questionId}", questionId).with(user(otherJunior)))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidQuestionIsRejectedWithAReadableValidationError() throws Exception {
        mockMvc.perform(post("/api/junior/questions")
                        .with(user(junior))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "questionBody": "Question text",
                                  "category": "ACADEMICS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.title").exists());
    }

    private PortalUserDetails createUser(String email, String name, String role) {
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
        long id = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE email = ?",
                Long.class,
                email
        );
        return new PortalUserDetails(id, name, email, "not-used", role);
    }
}
