CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    reg_no VARCHAR(30) NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_reg_no UNIQUE (reg_no),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS questions (
    question_id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    question_body TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (question_id),
    CONSTRAINT fk_questions_author FOREIGN KEY (author_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS answers (
    answer_id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    answer_body TEXT NOT NULL,
    is_verified_answer BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (answer_id),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (question_id),
    CONSTRAINT fk_answers_author FOREIGN KEY (author_id) REFERENCES users (user_id)
);

CREATE INDEX idx_questions_author_created ON questions (author_id, created_at);
CREATE INDEX idx_questions_created ON questions (created_at);
