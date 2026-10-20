-- ==========================================================
-- EduPath — Final Database Design (8 Tables)
-- Compatible with MySQL 8.x
-- Database: edupath_db
-- ==========================================================

CREATE DATABASE IF NOT EXISTS edupath_db;
USE edupath_db;

-- 1. Users and Roles
-- Consumed by: all modules for authentication, account identity, and role-based permissions
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reg_no VARCHAR(30) UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- JUNIOR / SENIOR / ALUMNI / ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Alumni Profiles
-- Relationship: One users row can have at most one alumni_profiles row;
-- alumni_id reuses the corresponding user_id
CREATE TABLE IF NOT EXISTS alumni_profiles (
    alumni_id BIGINT PRIMARY KEY,
    current_company VARCHAR(150),
    job_title VARCHAR(150),
    CONSTRAINT fk_alumni_user FOREIGN KEY (alumni_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Senior / Alumni Guidance
-- The application enforces whether SENIOR and/or ALUMNI users may publish
CREATE TABLE IF NOT EXISTS playbook_posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    markdown_content TEXT,
    category VARCHAR(40), -- Academics / Internships / Higher Studies / Placement
    status VARCHAR(20) DEFAULT 'PUBLISHED', -- DRAFT / PUBLISHED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playbook_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Questions and Answers
-- Note: guide_post_id is intentionally omitted
CREATE TABLE IF NOT EXISTS questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    question_body TEXT NOT NULL,
    category VARCHAR(40), -- Academics / Internships / Higher Studies / Placement
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    answer_body TEXT NOT NULL,
    is_verified_answer BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. AI Career Roadmaps
CREATE TABLE IF NOT EXISTS roadmaps (
    roadmap_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal VARCHAR(255),
    title VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmaps_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roadmap_nodes (
    node_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    semester INT,
    sequence_order INT DEFAULT 0,
    CONSTRAINT fk_nodes_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps(roadmap_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roadmap_edges (
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    PRIMARY KEY (source_node_id, target_node_id),
    CONSTRAINT fk_edges_source FOREIGN KEY (source_node_id) REFERENCES roadmap_nodes(node_id) ON DELETE CASCADE,
    CONSTRAINT fk_edges_target FOREIGN KEY (target_node_id) REFERENCES roadmap_nodes(node_id) ON DELETE CASCADE
);
