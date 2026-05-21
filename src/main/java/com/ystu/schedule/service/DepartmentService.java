package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.DepartmentRequest;
import com.ystu.schedule.dto.response.DepartmentResponse;
import com.ystu.schedule.entity.Department;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyService facultyService;

    public DepartmentService(DepartmentRepository departmentRepository, FacultyService facultyService) {
        this.departmentRepository = departmentRepository;
        this.facultyService = facultyService;
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> getByFaculty(Long facultyId) {
        return departmentRepository.findByFacultyId(facultyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }
        Faculty faculty = facultyService.findById(request.getFacultyId());
        Department department = new Department();
        department.setName(request.getName());
        department.setShortName(request.getShortName());
        department.setFaculty(faculty);
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findById(id);
        if (!department.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }
        Faculty faculty = facultyService.findById(request.getFacultyId());
        department.setName(request.getName());
        department.setShortName(request.getShortName());
        department.setFaculty(faculty);
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        departmentRepository.deleteById(id);
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setShortName(department.getShortName());
        response.setFacultyId(department.getFaculty().getId());
        response.setFacultyName(department.getFaculty().getName());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }
}
