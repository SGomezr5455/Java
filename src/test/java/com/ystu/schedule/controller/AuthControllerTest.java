package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.User;
import com.ystu.schedule.entity.enums.Role;
import com.ystu.schedule.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testadmin");
        user.setEmail("testadmin@ystu.ru");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        userRepository.saveAndFlush(user);

        User viewer = new User();
        viewer.setUsername("testviewer");
        viewer.setEmail("testviewer@ystu.ru");
        viewer.setPasswordHash(passwordEncoder.encode("password123"));
        viewer.setRole(Role.VIEWER);
        viewer.setEnabled(true);
        userRepository.saveAndFlush(viewer);
    }

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "newuser");
        body.put("email", "newuser@ystu.ru");
        body.put("password", "secret123");
        body.put("role", "EDITOR");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("EDITOR"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "testadmin");
        body.put("email", "other@ystu.ru");
        body.put("password", "secret123");
        body.put("role", "VIEWER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "brandnew");
        body.put("email", "testadmin@ystu.ru");
        body.put("password", "secret123");
        body.put("role", "VIEWER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "user2");
        body.put("email", "user2@ystu.ru");
        body.put("password", "123");
        body.put("role", "VIEWER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "testadmin");
        body.put("password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "testadmin");
        body.put("password", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUser_returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "nobody");
        body.put("password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedEndpoint_withAdminToken_returns200() throws Exception {
        String token = obtainToken("testadmin", "password123");

        mockMvc.perform(get("/api/v1/faculties")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/faculties"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteEndpoint_withViewerToken_returns403() throws Exception {
        String token = obtainToken("testviewer", "password123");

        mockMvc.perform(post("/api/v1/faculties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Тест\"}"))
                .andExpect(status().isForbidden());
    }

    private String obtainToken(String username, String password) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
