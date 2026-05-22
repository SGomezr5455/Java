package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.LessonRequest;
import com.ystu.schedule.dto.response.LessonResponse;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.entity.Lesson;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.enums.LessonType;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SubjectService subjectService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private RoomService roomService;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private LessonService lessonService;

    private Lesson lesson;
    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setId(1L);
        subject.setName("Java");

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubject(subject);
        lesson.setLessonType(LessonType.LECTURE);
        lesson.setStartTime(LocalDateTime.of(2026, 9, 1, 8, 0));
        lesson.setEndTime(LocalDateTime.of(2026, 9, 1, 9, 30));
        lesson.setWeekNumber(1);
        lesson.setIsDistant(false);
    }

    @Test
    void getAll_returnsAllLessons() {
        when(lessonRepository.findAll()).thenReturn(List.of(lesson));

        List<LessonResponse> result = lessonService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubjectName()).isEqualTo("Java");
    }

    @Test
    void getByGroup_returnsFilteredLessons() {
        when(lessonRepository.findByGroups_Id(1L)).thenReturn(List.of(lesson));

        List<LessonResponse> result = lessonService.getByGroup(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getByTeacher_returnsFilteredLessons() {
        when(lessonRepository.findByTeacherId(1L)).thenReturn(List.of());

        List<LessonResponse> result = lessonService.getByTeacher(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getByWeek_returnsFilteredLessons() {
        when(lessonRepository.findByWeekNumber(1)).thenReturn(List.of(lesson));

        List<LessonResponse> result = lessonService.getByWeek(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_existingId_returnsLesson() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        LessonResponse result = lessonService.getById(1L);

        assertThat(result.getLessonType()).isEqualTo(LessonType.LECTURE);
    }

    @Test
    void getById_missingId_throwsNotFoundException() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validRequest_savesLesson() {
        LessonRequest request = new LessonRequest();
        request.setSubjectId(1L);
        request.setLessonType(LessonType.LAB);
        request.setStartTime(LocalDateTime.of(2026, 9, 2, 10, 0));
        request.setEndTime(LocalDateTime.of(2026, 9, 2, 11, 30));
        request.setIsDistant(false);

        when(subjectService.findById(1L)).thenReturn(subject);
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> {
            Lesson saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        LessonResponse result = lessonService.create(request);

        assertThat(result).isNotNull();
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void addGroup_addsGroupToLesson() {
        Group group = new Group();
        group.setId(5L);
        group.setName("ИИТ-11");
        group.setCourseYear(1);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(groupService.findById(5L)).thenReturn(group);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        lessonService.addGroup(1L, 5L);

        assertThat(lesson.getGroups()).contains(group);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        lessonService.delete(1L);

        verify(lessonRepository).deleteById(1L);
    }
}
