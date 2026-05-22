package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.Department;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.entity.Subject;
import com.ystu.schedule.entity.Teacher;
import com.ystu.schedule.repository.DepartmentRepository;
import com.ystu.schedule.repository.FacultyRepository;
import com.ystu.schedule.repository.SubjectRepository;
import com.ystu.schedule.repository.TeacherRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private Teacher savedTeacher;
    private Department savedDepartment;
    private Subject savedSubject;

    @BeforeEach
    void setUp() {
        Faculty faculty = new Faculty();
        faculty.setName("ФИТ-учит");
        savedDepartment = new Department();
        savedDepartment.setName("Кафедра информатики-т");
        savedDepartment.setShortName("КИ");
        savedDepartment.setFaculty(facultyRepository.saveAndFlush(faculty));
        savedDepartment = departmentRepository.saveAndFlush(savedDepartment);

        savedSubject = new Subject();
        savedSubject.setName("Java-тест");
        savedSubject.setCreditHours(72);
        savedSubject = subjectRepository.saveAndFlush(savedSubject);

        Teacher teacher = new Teacher();
        teacher.setFirstName("Иван");
        teacher.setLastName("Иванов");
        teacher.setEmail("ivanov-test@ystu.ru");
        teacher.setDepartment(savedDepartment);
        savedTeacher = teacherRepository.saveAndFlush(teacher);
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/v1/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_withDepartmentFilter_returnsFilteredTeachers() throws Exception {
        mockMvc.perform(get("/api/v1/teachers")
                        .param("departmentId", savedDepartment.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Иванов"));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/{id}", savedTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivanov-test@ystu.ru"));
    }

    @Test
    void getById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Пётр");
        body.put("lastName", "Петров");
        body.put("email", "petrov-new@ystu.ru");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastName").value("Петров"));
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Другой");
        body.put("lastName", "Человек");
        body.put("email", "ivanov-test@ystu.ru");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_blankFirstName_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "");
        body.put("lastName", "Тест");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingId_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Иван");
        body.put("lastName", "Сидоров");
        body.put("email", "sidorov@ystu.ru");

        mockMvc.perform(put("/api/v1/teachers/{id}", savedTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Сидоров"));
    }

    @Test
    void addSubject_linksSubjectToTeacher() throws Exception {
        mockMvc.perform(post("/api/v1/teachers/{id}/subjects/{subjectId}",
                        savedTeacher.getId(), savedSubject.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void removeSubject_missingTeacher_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/teachers/999999/subjects/{subjectId}",
                        savedSubject.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/teachers/{id}", savedTeacher.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/teachers/999999"))
                .andExpect(status().isNotFound());
    }
}
