package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.TeacherRequest;
import com.ystu.schedule.dto.response.TeacherResponse;
import com.ystu.schedule.entity.Department;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.Teacher;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentService departmentService;
    private final SubjectService subjectService;

    public TeacherService(TeacherRepository teacherRepository,
                          DepartmentService departmentService,
                          SubjectService subjectService) {
        this.teacherRepository = teacherRepository;
        this.departmentService = departmentService;
        this.subjectService = subjectService;
    }

    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherResponse> getByDepartment(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        if (request.getEmail() != null && teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Teacher with email '" + request.getEmail() + "' already exists");
        }
        Teacher teacher = new Teacher();
        fillFields(teacher, request);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = findById(id);
        if (request.getEmail() != null
                && !request.getEmail().equals(teacher.getEmail())
                && teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Teacher with email '" + request.getEmail() + "' already exists");
        }
        fillFields(teacher, request);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        teacherRepository.deleteById(id);
    }

    @Transactional
    public TeacherResponse addSubject(Long teacherId, Long subjectId) {
        Teacher teacher = findById(teacherId);
        Subject subject = subjectService.findById(subjectId);
        teacher.getSubjects().add(subject);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse removeSubject(Long teacherId, Long subjectId) {
        Teacher teacher = findById(teacherId);
        Subject subject = subjectService.findById(subjectId);
        teacher.getSubjects().remove(subject);
        return toResponse(teacherRepository.save(teacher));
    }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
    }

    private void fillFields(Teacher teacher, TeacherRequest request) {
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setMiddleName(request.getMiddleName());
        teacher.setEmail(request.getEmail());
        teacher.setAcademicTitle(request.getAcademicTitle());
        if (request.getDepartmentId() != null) {
            Department department = departmentService.findById(request.getDepartmentId());
            teacher.setDepartment(department);
        } else {
            teacher.setDepartment(null);
        }
    }

    private TeacherResponse toResponse(Teacher teacher) {
        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setFirstName(teacher.getFirstName());
        response.setLastName(teacher.getLastName());
        response.setMiddleName(teacher.getMiddleName());
        response.setEmail(teacher.getEmail());
        response.setAcademicTitle(teacher.getAcademicTitle());
        if (teacher.getDepartment() != null) {
            response.setDepartmentId(teacher.getDepartment().getId());
            response.setDepartmentName(teacher.getDepartment().getName());
        }
        response.setCreatedAt(teacher.getCreatedAt());
        response.setUpdatedAt(teacher.getUpdatedAt());
        return response;
    }
}
