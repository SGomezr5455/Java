package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.repository.FacultyRepository;
import com.ystu.schedule.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private Faculty savedFaculty;
    private Group savedGroup;

    @BeforeEach
    void setUp() {
        Faculty faculty = new Faculty();
        faculty.setName("ФИТ-тест");
        faculty.setShortName("ФИТ");
        savedFaculty = facultyRepository.saveAndFlush(faculty);

        Group group = new Group();
        group.setName("ИИТ-11");
        group.setCourseYear(1);
        group.setFaculty(savedFaculty);
        savedGroup = groupRepository.saveAndFlush(group);
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_withFacultyFilter_returnsFilteredGroups() throws Exception {
        mockMvc.perform(get("/api/v1/groups").param("facultyId", savedFaculty.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ИИТ-11"));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{id}", savedGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ИИТ-11"))
                .andExpect(jsonPath("$.courseYear").value(1));
    }

    @Test
    void getById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/groups/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ИИТ-12");
        body.put("courseYear", 2);
        body.put("facultyId", savedFaculty.getId());

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ИИТ-12"));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ИИТ-11");
        body.put("courseYear", 1);

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "");

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingId_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ИИТ-11м");
        body.put("courseYear", 2);

        mockMvc.perform(put("/api/v1/groups/{id}", savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseYear").value(2));
    }

    @Test
    void update_missingId_returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "НесуществующаяГруппа");
        body.put("courseYear", 1);

        mockMvc.perform(put("/api/v1/groups/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/groups/{id}", savedGroup.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/groups/999999"))
                .andExpect(status().isNotFound());
    }
}
