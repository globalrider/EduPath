package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRegNo(String regNo);

    List<User> findByRole(Role role);
}
