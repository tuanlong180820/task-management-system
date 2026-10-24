
package com.taskmanagement.repository;

import com.taskmanagement.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository
        extends JpaRepository<Setting, Long> {

    Optional<Setting> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
}

