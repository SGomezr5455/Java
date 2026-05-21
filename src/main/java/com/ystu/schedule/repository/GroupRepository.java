package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByFacultyId(Long facultyId);

    Optional<Group> findByName(String name);

    boolean existsByName(String name);
}
