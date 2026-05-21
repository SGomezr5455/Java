package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.LessonRequest;
import com.ystu.schedule.dto.response.LessonResponse;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.entity.Lesson;
import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.Teacher;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectService subjectService;
    private final TeacherService teacherService;
    private final RoomService roomService;
    private final GroupService groupService;

    public LessonService(LessonRepository lessonRepository,
                         SubjectService subjectService,
                         TeacherService teacherService,
                         RoomService roomService,
                         GroupService groupService) {
        this.lessonRepository = lessonRepository;
        this.subjectService = subjectService;
        this.teacherService = teacherService;
        this.roomService = roomService;
        this.groupService = groupService;
    }

    public List<LessonResponse> getAll() {
        return lessonRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getByGroup(Long groupId) {
        return lessonRepository.findByGroups_Id(groupId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getByTeacher(Long teacherId) {
        return lessonRepository.findByTeacherId(teacherId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getByRoom(Long roomId) {
        return lessonRepository.findByRoomId(roomId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getByWeek(Integer weekNumber) {
        return lessonRepository.findByWeekNumber(weekNumber).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public LessonResponse create(LessonRequest request) {
        Lesson lesson = new Lesson();
        fillFields(lesson, request);
        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse update(Long id, LessonRequest request) {
        Lesson lesson = findById(id);
        fillFields(lesson, request);
        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        lessonRepository.deleteById(id);
    }

    @Transactional
    public LessonResponse addGroup(Long lessonId, Long groupId) {
        Lesson lesson = findById(lessonId);
        Group group = groupService.findById(groupId);
        lesson.getGroups().add(group);
        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse removeGroup(Long lessonId, Long groupId) {
        Lesson lesson = findById(lessonId);
        Group group = groupService.findById(groupId);
        lesson.getGroups().remove(group);
        return toResponse(lessonRepository.save(lesson));
    }

    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    private void fillFields(Lesson lesson, LessonRequest request) {
        Subject subject = subjectService.findById(request.getSubjectId());
        lesson.setSubject(subject);
        if (request.getTeacherId() != null) {
            Teacher teacher = teacherService.findById(request.getTeacherId());
            lesson.setTeacher(teacher);
        } else {
            lesson.setTeacher(null);
        }
        if (request.getRoomId() != null) {
            Room room = roomService.findById(request.getRoomId());
            lesson.setRoom(room);
        } else {
            lesson.setRoom(null);
        }
        lesson.setLessonType(request.getLessonType());
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setPairNumber(request.getPairNumber());
        lesson.setWeekNumber(request.getWeekNumber());
        lesson.setSubgroup(request.getSubgroup());
        lesson.setIsDistant(request.getIsDistant() != null ? request.getIsDistant() : false);
        lesson.setComment(request.getComment());
    }

    private LessonResponse toResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setSubjectId(lesson.getSubject().getId());
        response.setSubjectName(lesson.getSubject().getName());
        if (lesson.getTeacher() != null) {
            response.setTeacherId(lesson.getTeacher().getId());
            response.setTeacherName(lesson.getTeacher().getLastName() + " "
                    + lesson.getTeacher().getFirstName());
        }
        if (lesson.getRoom() != null) {
            response.setRoomId(lesson.getRoom().getId());
            response.setRoomNumber(lesson.getRoom().getNumber());
        }
        Set<Long> groupIds = lesson.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
        response.setGroupIds(groupIds);
        response.setLessonType(lesson.getLessonType());
        response.setStartTime(lesson.getStartTime());
        response.setEndTime(lesson.getEndTime());
        response.setPairNumber(lesson.getPairNumber());
        response.setWeekNumber(lesson.getWeekNumber());
        response.setSubgroup(lesson.getSubgroup());
        response.setIsDistant(lesson.getIsDistant());
        response.setComment(lesson.getComment());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        return response;
    }
}
