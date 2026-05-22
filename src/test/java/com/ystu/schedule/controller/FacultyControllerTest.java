package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.repository.FacultyRepository;
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
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FacultyRepository facultyRepository;

    private Faculty savedFaculty;

    @BeforeEach
    void setUp() {
        Faculty faculty = new Faculty();
        faculty.setName("ФИТ");
        faculty.setShortName("ФИТ");
        savedFaculty = facultyRepository.saveAndFlush(faculty);
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/v1/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/faculties/{id}", savedFaculty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ФИТ"));
    }

    @Test
    void getById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/faculties/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ФХТИЭ");
        body.put("shortName", "ФХ");

        mockMvc.perform(post("/api/v1/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ФХТИЭ"));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ФИТ");

        mockMvc.perform(post("/api/v1/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_blankName_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "");

        mockMvc.perform(post("/api/v1/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingId_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "ФИТ обновлённый");
        body.put("shortName", "ФИТ+");

        mockMvc.perform(put("/api/v1/faculties/{id}", savedFaculty.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ФИТ обновлённый"));
    }

    @Test
    void update_missingId_returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Несуществующий");

        mockMvc.perform(put("/api/v1/faculties/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/faculties/{id}", savedFaculty.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/faculties/999999"))
                .andExpect(status().isNotFound());
    }
}
