package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.entity.Lesson;
import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.enums.LessonType;
import com.ystu.schedule.entity.enums.RoomType;
import com.ystu.schedule.repository.GroupRepository;
import com.ystu.schedule.repository.LessonRepository;
import com.ystu.schedule.repository.RoomRepository;
import com.ystu.schedule.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Subject savedSubject;
    private Room savedRoom;
    private Group savedGroup;
    private Lesson savedLesson;

    @BeforeEach
    void setUp() {
        savedSubject = new Subject();
        savedSubject.setName("Java-урок");
        savedSubject.setCreditHours(72);
        savedSubject = subjectRepository.saveAndFlush(savedSubject);

        savedRoom = new Room();
        savedRoom.setNumber("А-101");
        savedRoom.setCapacity(30);
        savedRoom.setRoomType(RoomType.LECTURE_HALL);
        savedRoom = roomRepository.saveAndFlush(savedRoom);

        savedGroup = new Group();
        savedGroup.setName("ИИТ-тест");
        savedGroup.setCourseYear(1);
        savedGroup = groupRepository.saveAndFlush(savedGroup);

        Lesson lesson = new Lesson();
        lesson.setSubject(savedSubject);
        lesson.setLessonType(LessonType.LECTURE);
        lesson.setStartTime(LocalDateTime.of(2026, 9, 1, 8, 0));
        lesson.setEndTime(LocalDateTime.of(2026, 9, 1, 9, 30));
        lesson.setWeekNumber(1);
        lesson.setIsDistant(false);
        savedLesson = lessonRepository.saveAndFlush(lesson);
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/v1/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_withWeekNumberFilter_returnsFilteredLessons() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").param("weekNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weekNumber").value(1));
    }

    @Test
    void getAll_withGroupFilter_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").param("groupId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_withRoomFilter_returnsFilteredLessons() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").param("roomId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/{id}", savedLesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Java-урок"));
    }

    @Test
    void getById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("subjectId", savedSubject.getId());
        body.put("lessonType", "LAB");
        body.put("startTime", "2026-09-02T10:00:00");
        body.put("endTime", "2026-09-02T11:30:00");
        body.put("weekNumber", 2);
        body.put("isDistant", false);

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lessonType").value("LAB"));
    }

    @Test
    void create_missingSubjectId_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("lessonType", "LAB");
        body.put("startTime", "2026-09-02T10:00:00");
        body.put("endTime", "2026-09-02T11:30:00");

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addGroup_addsGroupToLesson() throws Exception {
        mockMvc.perform(post("/api/v1/lessons/{id}/groups/{groupId}",
                        savedLesson.getId(), savedGroup.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void addGroup_missingLesson_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/lessons/999999/groups/{groupId}",
                        savedGroup.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingId_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("subjectId", savedSubject.getId());
        body.put("lessonType", "PRACTICE");
        body.put("startTime", "2026-09-03T12:00:00");
        body.put("endTime", "2026-09-03T13:30:00");
        body.put("weekNumber", 3);
        body.put("isDistant", true);

        mockMvc.perform(put("/api/v1/lessons/{id}", savedLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonType").value("PRACTICE"));
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/{id}", savedLesson.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/999999"))
                .andExpect(status().isNotFound());
    }
}
