package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.DepartmentRequest;
import com.ystu.schedule.dto.response.DepartmentResponse;
import com.ystu.schedule.entity.Department;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FacultyService facultyService;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("ФИТ");

        department = new Department();
        department.setId(1L);
        department.setName("Кафедра информатики");
        department.setShortName("КИ");
        department.setFaculty(faculty);
    }

    @Test
    void getAll_returnsAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<DepartmentResponse> result = departmentService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFacultyName()).isEqualTo("ФИТ");
    }

    @Test
    void getByFaculty_returnsFilteredDepartments() {
        when(departmentRepository.findByFacultyId(1L)).thenReturn(List.of(department));

        List<DepartmentResponse> result = departmentService.getByFaculty(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_existingId_returnsDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        DepartmentResponse result = departmentService.getById(1L);

        assertThat(result.getName()).isEqualTo("Кафедра информатики");
    }

    @Test
    void getById_missingId_throwsNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_uniqueName_savesDepartment() {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Кафедра математики");
        request.setFacultyId(1L);

        when(departmentRepository.existsByName("Кафедра математики")).thenReturn(false);
        when(facultyService.findById(1L)).thenReturn(faculty);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            Department saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        DepartmentResponse result = departmentService.create(request);

        assertThat(result.getName()).isEqualTo("Кафедра математики");
    }

    @Test
    void create_duplicateName_throwsDuplicateException() {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Кафедра информатики");
        request.setFacultyId(1L);

        when(departmentRepository.existsByName("Кафедра информатики")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        departmentService.delete(1L);

        verify(departmentRepository).deleteById(1L);
    }
}
