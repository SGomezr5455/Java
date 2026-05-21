package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByName(String name);

    boolean existsByName(String name);
}
