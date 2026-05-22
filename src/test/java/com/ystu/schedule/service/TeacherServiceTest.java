package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.TeacherRequest;
import com.ystu.schedule.dto.response.TeacherResponse;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.Teacher;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Иван");
        teacher.setLastName("Иванов");
        teacher.setEmail("ivanov@ystu.ru");
    }

    @Test
    void getAll_returnsAllTeachers() {
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));

        List<TeacherResponse> result = teacherService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo("Иванов");
    }

    @Test
    void getById_existingId_returnsTeacher() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        TeacherResponse result = teacherService.getById(1L);

        assertThat(result.getEmail()).isEqualTo("ivanov@ystu.ru");
    }

    @Test
    void getById_missingId_throwsNotFoundException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_uniqueEmail_savesTeacher() {
        TeacherRequest request = new TeacherRequest();
        request.setFirstName("Пётр");
        request.setLastName("Петров");
        request.setEmail("petrov@ystu.ru");

        when(teacherRepository.existsByEmail("petrov@ystu.ru")).thenReturn(false);
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> {
            Teacher saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TeacherResponse result = teacherService.create(request);

        assertThat(result.getLastName()).isEqualTo("Петров");
    }

    @Test
    void create_duplicateEmail_throwsDuplicateException() {
        TeacherRequest request = new TeacherRequest();
        request.setFirstName("Другой");
        request.setLastName("Человек");
        request.setEmail("ivanov@ystu.ru");

        when(teacherRepository.existsByEmail("ivanov@ystu.ru")).thenReturn(true);

        assertThatThrownBy(() -> teacherService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addSubject_linksSubjectToTeacher() {
        Subject subject = new Subject();
        subject.setId(10L);
        subject.setName("Java");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectService.findById(10L)).thenReturn(subject);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        TeacherResponse result = teacherService.addSubject(1L, 10L);

        assertThat(teacher.getSubjects()).contains(subject);
        verify(teacherRepository).save(teacher);
    }

    @Test
    void removeSubject_unlinksSubjectFromTeacher() {
        Subject subject = new Subject();
        subject.setId(10L);
        subject.setName("Java");
        teacher.getSubjects().add(subject);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectService.findById(10L)).thenReturn(subject);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        teacherService.removeSubject(1L, 10L);

        assertThat(teacher.getSubjects()).doesNotContain(subject);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        teacherService.delete(1L);

        verify(teacherRepository).deleteById(1L);
    }
}
