
package com.taskmanagement.repository;

import com.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByIdDesc(
            String fullName,
            String email
    );

}
