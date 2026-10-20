package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.PlaybookRequest;
import com.example.edupath.demo.dto.PlaybookResponse;
import com.example.edupath.demo.exception.ResourceNotFoundException;
import com.example.edupath.demo.model.PlaybookPost;
import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;
import com.example.edupath.demo.repository.PlaybookRepository;
import com.example.edupath.demo.repository.UserRepository;
import com.example.edupath.demo.service.PlaybookService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaybookServiceImpl implements PlaybookService {

    private final PlaybookRepository playbookRepository;
    private final UserRepository userRepository;

    public PlaybookServiceImpl(PlaybookRepository playbookRepository, UserRepository userRepository) {
        this.playbookRepository = playbookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaybookResponse> getAllPlaybooks(String category, String status, String search) {
        List<PlaybookPost> posts;

        if (search != null && !search.isBlank()) {
            posts = playbookRepository.findByTitleContainingIgnoreCaseOrMarkdownContentContainingIgnoreCase(search, search);
        } else if (category != null && !category.equalsIgnoreCase("ALL") && !category.isBlank()) {
            posts = playbookRepository.findByCategoryIgnoreCase(category);
        } else if (status != null && !status.isBlank()) {
            posts = playbookRepository.findByStatus(status);
        } else {
            posts = playbookRepository.findAll();
        }

        return posts.stream()
                .map(PlaybookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlaybookResponse getPlaybookById(Long id) {
        PlaybookPost post = playbookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playbook guidance post not found with ID: " + id));
        return PlaybookResponse.fromEntity(post);
    }

    @Override
    public PlaybookResponse createPlaybook(String userEmail, PlaybookRequest request) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with email: " + userEmail));

        // Enforce role rule: SENIOR, ALUMNI, or ADMIN may publish playbook posts
        if (author.getRole() != Role.SENIOR && author.getRole() != Role.ALUMNI && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only SENIOR and ALUMNI members can publish guidance playbook posts");
        }

        PlaybookPost post = new PlaybookPost();
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setMarkdownContent(request.getMarkdownContent());
        post.setCategory(request.getCategory() != null ? request.getCategory() : "Academics");
        post.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "PUBLISHED");

        PlaybookPost saved = playbookRepository.save(post);
        return PlaybookResponse.fromEntity(saved);
    }

    @Override
    public PlaybookResponse updatePlaybook(Long id, String userEmail, boolean isAdmin, PlaybookRequest request) {
        PlaybookPost post = playbookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playbook post not found with ID: " + id));

        if (!isAdmin && !post.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only edit playbook guides that you authored");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.setTitle(request.getTitle());
        }
        if (request.getMarkdownContent() != null) {
            post.setMarkdownContent(request.getMarkdownContent());
        }
        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        PlaybookPost updated = playbookRepository.save(post);
        return PlaybookResponse.fromEntity(updated);
    }

    @Override
    public void deletePlaybook(Long id, String userEmail, boolean isAdmin) {
        PlaybookPost post = playbookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playbook post not found with ID: " + id));

        if (!isAdmin && !post.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only delete playbook guides that you authored");
        }

        playbookRepository.delete(post);
    }
}
