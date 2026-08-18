package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u FROM User u
    WHERE u.username LIKE CONCAT('%', :keyword, '%')
       OR u.email LIKE CONCAT('%', :keyword, '%')
""")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
