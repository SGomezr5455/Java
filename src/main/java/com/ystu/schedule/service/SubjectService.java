package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.SubjectRequest;
import com.ystu.schedule.dto.response.SubjectResponse;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SubjectResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Subject with name '" + request.getName() + "' already exists");
        }
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setShortName(request.getShortName());
        subject.setCreditHours(request.getCreditHours());
        return toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = findById(id);
        if (!subject.getName().equals(request.getName()) && subjectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Subject with name '" + request.getName() + "' already exists");
        }
        subject.setName(request.getName());
        subject.setShortName(request.getShortName());
        subject.setCreditHours(request.getCreditHours());
        return toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        subjectRepository.deleteById(id);
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    private SubjectResponse toResponse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setId(subject.getId());
        response.setName(subject.getName());
        response.setShortName(subject.getShortName());
        response.setCreditHours(subject.getCreditHours());
        response.setCreatedAt(subject.getCreatedAt());
        response.setUpdatedAt(subject.getUpdatedAt());
        return response;
    }
}
