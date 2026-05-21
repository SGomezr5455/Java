package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.RoomRequest;
import com.ystu.schedule.dto.response.RoomResponse;
import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.enums.RoomType;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomResponse> getAll() {
        return roomRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> getByType(RoomType roomType) {
        return roomRepository.findByRoomType(roomType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByNumber(request.getNumber())) {
            throw new DuplicateResourceException("Room with number '" + request.getNumber() + "' already exists");
        }
        Room room = new Room();
        room.setNumber(request.getNumber());
        room.setBuilding(request.getBuilding());
        room.setCapacity(request.getCapacity());
        room.setRoomType(request.getRoomType());
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = findById(id);
        if (!room.getNumber().equals(request.getNumber()) && roomRepository.existsByNumber(request.getNumber())) {
            throw new DuplicateResourceException("Room with number '" + request.getNumber() + "' already exists");
        }
        room.setNumber(request.getNumber());
        room.setBuilding(request.getBuilding());
        room.setCapacity(request.getCapacity());
        room.setRoomType(request.getRoomType());
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        roomRepository.deleteById(id);
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    private RoomResponse toResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setNumber(room.getNumber());
        response.setBuilding(room.getBuilding());
        response.setCapacity(room.getCapacity());
        response.setRoomType(room.getRoomType());
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());
        return response;
    }
}
