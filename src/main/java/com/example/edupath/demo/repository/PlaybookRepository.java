package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.PlaybookPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaybookRepository extends JpaRepository<PlaybookPost, Long> {

    List<PlaybookPost> findByCategoryIgnoreCase(String category);

    List<PlaybookPost> findByStatus(String status);

    List<PlaybookPost> findByAuthor_Id(Long authorId);

    List<PlaybookPost> findByTitleContainingIgnoreCaseOrMarkdownContentContainingIgnoreCase(String title, String content);
}
