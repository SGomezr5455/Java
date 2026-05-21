package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.GroupRequest;
import com.ystu.schedule.dto.response.GroupResponse;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final FacultyService facultyService;

    public GroupService(GroupRepository groupRepository, FacultyService facultyService) {
        this.groupRepository = groupRepository;
        this.facultyService = facultyService;
    }

    public List<GroupResponse> getAll() {
        return groupRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getByFaculty(Long facultyId) {
        return groupRepository.findByFacultyId(facultyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GroupResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public GroupResponse create(GroupRequest request) {
        if (groupRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Group with name '" + request.getName() + "' already exists");
        }
        Group group = new Group();
        group.setName(request.getName());
        group.setCourseYear(request.getCourseYear());
        if (request.getFacultyId() != null) {
            Faculty faculty = facultyService.findById(request.getFacultyId());
            group.setFaculty(faculty);
        }
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public GroupResponse update(Long id, GroupRequest request) {
        Group group = findById(id);
        if (!group.getName().equals(request.getName()) && groupRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Group with name '" + request.getName() + "' already exists");
        }
        group.setName(request.getName());
        group.setCourseYear(request.getCourseYear());
        if (request.getFacultyId() != null) {
            Faculty faculty = facultyService.findById(request.getFacultyId());
            group.setFaculty(faculty);
        } else {
            group.setFaculty(null);
        }
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        groupRepository.deleteById(id);
    }

    public Group findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    private GroupResponse toResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCourseYear(group.getCourseYear());
        if (group.getFaculty() != null) {
            response.setFacultyId(group.getFaculty().getId());
            response.setFacultyName(group.getFaculty().getName());
        }
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        return response;
    }
}
