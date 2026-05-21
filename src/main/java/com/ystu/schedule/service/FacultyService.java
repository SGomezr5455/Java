package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.FacultyRequest;
import com.ystu.schedule.dto.response.FacultyResponse;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<FacultyResponse> getAll() {
        return facultyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FacultyResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        if (facultyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Faculty with name '" + request.getName() + "' already exists");
        }
        Faculty faculty = new Faculty();
        faculty.setName(request.getName());
        faculty.setShortName(request.getShortName());
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public FacultyResponse update(Long id, FacultyRequest request) {
        Faculty faculty = findById(id);
        if (!faculty.getName().equals(request.getName()) && facultyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Faculty with name '" + request.getName() + "' already exists");
        }
        faculty.setName(request.getName());
        faculty.setShortName(request.getShortName());
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        facultyRepository.deleteById(id);
    }

    public Faculty findById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }

    private FacultyResponse toResponse(Faculty faculty) {
        FacultyResponse response = new FacultyResponse();
        response.setId(faculty.getId());
        response.setName(faculty.getName());
        response.setShortName(faculty.getShortName());
        response.setCreatedAt(faculty.getCreatedAt());
        response.setUpdatedAt(faculty.getUpdatedAt());
        return response;
    }
}
