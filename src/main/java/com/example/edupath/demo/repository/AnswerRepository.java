package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestion_QuestionIdOrderByCreatedAtAsc(Long questionId);

    List<Answer> findByAuthor_Id(Long authorId);
}
