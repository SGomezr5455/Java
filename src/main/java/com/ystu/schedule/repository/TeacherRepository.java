package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    List<Teacher> findByDepartmentId(Long departmentId);

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);
}
