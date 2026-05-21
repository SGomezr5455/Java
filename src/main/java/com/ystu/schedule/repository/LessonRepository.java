package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByGroups_Id(Long groupId);

    List<Lesson> findByTeacherId(Long teacherId);

    List<Lesson> findByRoomId(Long roomId);

    List<Lesson> findByWeekNumber(Integer weekNumber);
}
