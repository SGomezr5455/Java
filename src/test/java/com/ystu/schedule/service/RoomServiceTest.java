package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.RoomRequest;
import com.ystu.schedule.dto.response.RoomResponse;
import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.enums.RoomType;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setBuilding("Корпус А");
        room.setCapacity(30);
        room.setRoomType(RoomType.LECTURE_HALL);
    }

    @Test
    void getAll_returnsAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomResponse> result = roomService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumber()).isEqualTo("101");
    }

    @Test
    void getByType_returnsFilteredRooms() {
        when(roomRepository.findByRoomType(RoomType.LAB)).thenReturn(List.of());

        List<RoomResponse> result = roomService.getByType(RoomType.LAB);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_existingId_returnsRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomResponse result = roomService.getById(1L);

        assertThat(result.getCapacity()).isEqualTo(30);
        assertThat(result.getRoomType()).isEqualTo(RoomType.LECTURE_HALL);
    }

    @Test
    void getById_missingId_throwsNotFoundException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_uniqueNumber_savesRoom() {
        RoomRequest request = new RoomRequest();
        request.setNumber("202");
        request.setBuilding("Корпус Б");
        request.setCapacity(25);
        request.setRoomType(RoomType.SEMINAR_ROOM);

        when(roomRepository.existsByNumber("202")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        RoomResponse result = roomService.create(request);

        assertThat(result.getNumber()).isEqualTo("202");
    }

    @Test
    void create_duplicateNumber_throwsDuplicateException() {
        RoomRequest request = new RoomRequest();
        request.setNumber("101");

        when(roomRepository.existsByNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.delete(1L);

        verify(roomRepository).deleteById(1L);
    }
}
