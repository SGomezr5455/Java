package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.FacultyRequest;
import com.ystu.schedule.dto.response.FacultyResponse;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.FacultyRepository;
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
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private FacultyService facultyService;

    private Faculty faculty;

    @BeforeEach
    void setUp() {
        faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Факультет информационных технологий");
        faculty.setShortName("ФИТ");
    }

    @Test
    void getAll_returnsListOfFaculties() {
        when(facultyRepository.findAll()).thenReturn(List.of(faculty));

        List<FacultyResponse> result = facultyService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(faculty.getName());
    }

    @Test
    void getById_existingId_returnsFaculty() {
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));

        FacultyResponse result = facultyService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(faculty.getName());
    }

    @Test
    void getById_missingId_throwsResourceNotFoundException() {
        when(facultyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultyService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_uniqueName_returnsSavedFaculty() {
        FacultyRequest request = new FacultyRequest();
        request.setName("Новый факультет");
        request.setShortName("НФ");

        when(facultyRepository.existsByName(request.getName())).thenReturn(false);
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(inv -> {
            Faculty saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        FacultyResponse result = facultyService.create(request);

        assertThat(result.getName()).isEqualTo("Новый факультет");
        verify(facultyRepository).save(any(Faculty.class));
    }

    @Test
    void create_duplicateName_throwsDuplicateResourceException() {
        FacultyRequest request = new FacultyRequest();
        request.setName(faculty.getName());

        when(facultyRepository.existsByName(faculty.getName())).thenReturn(true);

        assertThatThrownBy(() -> facultyService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_existingId_updatesFields() {
        FacultyRequest request = new FacultyRequest();
        request.setName("Обновлённый факультет");
        request.setShortName("ОФ");

        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(facultyRepository.existsByName(request.getName())).thenReturn(false);
        when(facultyRepository.save(any(Faculty.class))).thenReturn(faculty);

        FacultyResponse result = facultyService.update(1L, request);

        assertThat(result).isNotNull();
        verify(facultyRepository).save(faculty);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));

        facultyService.delete(1L);

        verify(facultyRepository).deleteById(1L);
    }

    @Test
    void delete_missingId_throwsResourceNotFoundException() {
        when(facultyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultyService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(facultyRepository, never()).deleteById(any());
    }
}
