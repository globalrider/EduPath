package com.example.edupath.demo.config;

import com.example.edupath.demo.model.*;
import com.example.edupath.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            AlumniProfileRepository alumniProfileRepository,
            PlaybookRepository playbookRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            RoadmapRepository roadmapRepository,
            RoadmapNodeRepository roadmapNodeRepository,
            RoadmapEdgeRepository roadmapEdgeRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("EduPath database is already initialized. Skipping seed.");
                return;
            }

            log.info("Seeding EduPath Final Database Design (8 tables, MySQL / H2 compatible)...");

            // 1. Seed Users & Roles with BCrypt hashed passwords
            String hashPass = passwordEncoder.encode("password123");
            String hashAdmin = passwordEncoder.encode("admin123");

            User senior1 = userRepository.save(new User("21CS010", "Aravind R", "aravind@edupath.edu", hashPass, Role.SENIOR));
            User senior2 = userRepository.save(new User("22CS045", "Karthik S", "karthik@edupath.edu", hashPass, Role.SENIOR));
            User alumni = userRepository.save(new User("19CS002", "Deepa M", "deepa@amazon.com", hashPass, Role.ALUMNI));
            User junior = userRepository.save(new User("23CS101", "Vignesh K", "vignesh@edupath.edu", hashPass, Role.JUNIOR));
            User admin = userRepository.save(new User("ADM001", "Administrator", "admin@edupath.edu", hashAdmin, Role.ADMIN));

            log.info("Seeded 5 users across JUNIOR, SENIOR, ALUMNI, and ADMIN roles.");

            // 2. Seed Alumni Profiles (table: alumni_profiles)
            AlumniProfile alumniProfile = new AlumniProfile(alumni, "Amazon", "Senior Software Development Engineer");
            alumniProfile.setAlumniId(alumni.getId());
            alumniProfileRepository.save(alumniProfile);
            log.info("Seeded alumni profile for: {}", alumni.getEmail());

            // 3. Seed Senior / Alumni Guidance (table: playbook_posts)
            PlaybookPost post1 = new PlaybookPost(
                senior1,
                "Semester 1 & 2 Blueprint: Computer Science Fundamentals",
                "Don't ignore Engineering Mathematics: it directly impacts 3rd Sem algorithmic efficiency. Focus heavily on C basics, pointers, dynamic memory allocation (`malloc`/`free`), and memory layout. Start solving basic logic puzzles early to build strong fundamentals for Data Structures.",
                "Academics",
                "PUBLISHED"
            );

            PlaybookPost post2 = new PlaybookPost(
                senior2,
                "Off-Campus Internship Strategy & Resume Building",
                "1. Build 2 deep full-stack projects rather than 10 tutorial clones.\n2. Ensure your resume has quantifiable impact (e.g. 'Optimized database queries decreasing latency by 45%').\n3. Start cold reaching out to tech leads on LinkedIn by early January.",
                "Internships",
                "PUBLISHED"
            );

            PlaybookPost post3 = new PlaybookPost(
                alumni,
                "Mastering Placement Coding Rounds & System Design",
                "Amazon and top tech interviews focus on DSA rigor (Trees, Graphs, Sliding Window) and Clean Code. Emphasize modular object-oriented design and demonstrate solid database indexing and concurrency knowledge.",
                "Placement",
                "PUBLISHED"
            );

            playbookRepository.saveAll(List.of(post1, post2, post3));
            log.info("Seeded guidance playbook posts.");

            // 4. Seed Questions and Answers (tables: questions, answers)
            Question q1 = questionRepository.save(new Question(
                junior,
                "How to prepare for Data Structures in 3rd Semester?",
                "I am entering 3rd semester and feel overwhelmed with pointers and dynamic memory. What is the recommended roadmap to prepare for tree traversals and dynamic programming?",
                "Academics"
            ));

            Question q2 = questionRepository.save(new Question(
                junior,
                "When should a 2nd year student start applying for internships?",
                "Should I wait until I finish full-stack web dev before applying, or start cold emailing with core Java skills?",
                "Internships"
            ));

            Answer a1 = new Answer(
                q1,
                senior1,
                "Start with Striver's A2Z DSA sheet or NeetCode 150. Focus on Arrays and LinkedLists for the first 3 weeks. Pointers in C/C++ or references in Java will become natural once you visualize memory with diagrams.",
                false
            );

            Answer a2 = new Answer(
                q1,
                alumni,
                "Verified Industry Advice: Do not jump straight to DP. Master recursion, divide-and-conquer, and binary search first. Practice writing clean code without IDE autocomplete, as whiteboard/CoderPad rounds test syntax confidence.",
                true // Verified Answer Badge
            );

            Answer a3 = new Answer(
                q2,
                senior2,
                "Start preparing your resume in Sem 3 and start applying in Sem 4 (January-March) for summer internships. Having at least 1 deployed Spring Boot REST API project will set you apart from 90% of applicants.",
                false
            );

            answerRepository.saveAll(List.of(a1, a2, a3));
            log.info("Seeded questions and verified answers.");

            // 5. Seed AI Career Roadmaps, Nodes, and Edges (tables: roadmaps, roadmap_nodes, roadmap_edges)
            Roadmap roadmap = new Roadmap(junior, "Full Stack Java Developer", "AI Career Path: Full Stack Java Developer");
            Roadmap savedRoadmap = roadmapRepository.save(roadmap);

            RoadmapNode n1 = new RoadmapNode(savedRoadmap, "1. C & Programming Foundations", "Master variables, pointers, dynamic memory allocation, and time complexity analysis.", 1, 1);
            RoadmapNode n2 = new RoadmapNode(savedRoadmap, "2. Data Structures & Java OOP", "Classes, Interfaces, Generics, Collections Framework, Trees, and Graphs.", 2, 2);
            RoadmapNode n3 = new RoadmapNode(savedRoadmap, "3. Relational Database Design & MySQL", "Schema normalization (3NF), foreign keys, ACID transactions, and indexing.", 3, 3);
            RoadmapNode n4 = new RoadmapNode(savedRoadmap, "4. Spring Boot & JPA Architecture", "Dependency Injection, Spring Data JPA entities, repositories, and REST controllers.", 4, 4);
            RoadmapNode n5 = new RoadmapNode(savedRoadmap, "5. JWT Authentication & Spring Security", "Stateless filter chains, HMAC-SHA256 tokens, and role-based authorization.", 5, 5);
            RoadmapNode n6 = new RoadmapNode(savedRoadmap, "6. Modern Frontend UI (React & Tailwind)", "Component hierarchies, asynchronous fetch requests, and state management.", 6, 6);
            RoadmapNode n7 = new RoadmapNode(savedRoadmap, "7. Docker & Cloud Deployment", "Multi-stage Docker builds, containerizing services, and cloud hosting.", 7, 7);

            List<RoadmapNode> savedNodes = roadmapNodeRepository.saveAll(List.of(n1, n2, n3, n4, n5, n6, n7));

            // Directed prerequisite graph edges
            RoadmapEdge e1 = new RoadmapEdge(savedNodes.get(0), savedNodes.get(1)); // n1 -> n2
            RoadmapEdge e2 = new RoadmapEdge(savedNodes.get(1), savedNodes.get(3)); // n2 -> n4
            RoadmapEdge e3 = new RoadmapEdge(savedNodes.get(2), savedNodes.get(3)); // n3 -> n4
            RoadmapEdge e4 = new RoadmapEdge(savedNodes.get(3), savedNodes.get(4)); // n4 -> n5
            RoadmapEdge e5 = new RoadmapEdge(savedNodes.get(4), savedNodes.get(5)); // n5 -> n6
            RoadmapEdge e6 = new RoadmapEdge(savedNodes.get(3), savedNodes.get(6)); // n4 -> n7

            roadmapEdgeRepository.saveAll(List.of(e1, e2, e3, e4, e5, e6));
            log.info("Seeded AI Career Roadmap with 7 milestone nodes and 6 directed prerequisite edges.");
            log.info("EduPath final database initialization completed successfully.");
        };
    }
}
