package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.SubjectRequest;
import com.ystu.schedule.dto.response.SubjectResponse;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.SubjectRepository;
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
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setId(1L);
        subject.setName("Основы программирования");
        subject.setShortName("ОП");
        subject.setCreditHours(72);
    }

    @Test
    void getAll_returnsAllSubjects() {
        when(subjectRepository.findAll()).thenReturn(List.of(subject));

        List<SubjectResponse> result = subjectService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Основы программирования");
    }

    @Test
    void getById_existingId_returnsSubject() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        SubjectResponse result = subjectService.getById(1L);

        assertThat(result.getCreditHours()).isEqualTo(72);
    }

    @Test
    void getById_missingId_throwsNotFoundException() {
        when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subjectService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_uniqueName_savesSubject() {
        SubjectRequest request = new SubjectRequest();
        request.setName("Математика");
        request.setCreditHours(36);

        when(subjectRepository.existsByName("Математика")).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(inv -> {
            Subject saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        SubjectResponse result = subjectService.create(request);

        assertThat(result.getName()).isEqualTo("Математика");
    }

    @Test
    void create_duplicateName_throwsDuplicateException() {
        SubjectRequest request = new SubjectRequest();
        request.setName("Основы программирования");

        when(subjectRepository.existsByName(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> subjectService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(subjectRepository, never()).save(any());
    }

    @Test
    void update_existingSubject_updatesSuccessfully() {
        SubjectRequest request = new SubjectRequest();
        request.setName("Алгоритмы");
        request.setCreditHours(54);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByName("Алгоритмы")).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);

        SubjectResponse result = subjectService.update(1L, request);

        assertThat(result).isNotNull();
        verify(subjectRepository).save(subject);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        subjectService.delete(1L);

        verify(subjectRepository).deleteById(1L);
    }
}
