package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCategoryIgnoreCase(String category);

    List<Question> findByAuthor_Id(Long authorId);

    List<Question> findByTitleContainingIgnoreCaseOrQuestionBodyContainingIgnoreCase(String title, String body);
}
