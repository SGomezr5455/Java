package com.ystu.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.enums.RoomType;
import com.ystu.schedule.repository.RoomRepository;
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
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    private Room savedRoom;

    @BeforeEach
    void setUp() {
        Room room = new Room();
        room.setNumber("101");
        room.setBuilding("Корпус А");
        room.setCapacity(30);
        room.setRoomType(RoomType.LECTURE_HALL);
        savedRoom = roomRepository.saveAndFlush(room);
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_withTypeFilter_returnsFilteredRooms() throws Exception {
        mockMvc.perform(get("/api/v1/rooms").param("type", "LECTURE_HALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("101"));
    }

    @Test
    void getAll_withTypeFilterNoMatch_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/rooms").param("type", "GYM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/{id}", savedRoom.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("101"))
                .andExpect(jsonPath("$.capacity").value(30));
    }

    @Test
    void getById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("number", "202");
        body.put("building", "Корпус Б");
        body.put("capacity", 25);
        body.put("roomType", "SEMINAR_ROOM");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("202"));
    }

    @Test
    void create_duplicateNumber_returns409() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("number", "101");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_blankNumber_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("number", "");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingId_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("number", "101-А");
        body.put("building", "Корпус А");
        body.put("capacity", 40);
        body.put("roomType", "LECTURE_HALL");

        mockMvc.perform(put("/api/v1/rooms/{id}", savedRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(40));
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/rooms/{id}", savedRoom.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/rooms/999999"))
                .andExpect(status().isNotFound());
    }
}
