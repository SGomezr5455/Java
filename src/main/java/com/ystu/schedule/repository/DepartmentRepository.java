package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByFacultyId(Long facultyId);

    Optional<Department> findByName(String name);

    boolean existsByName(String name);
}
