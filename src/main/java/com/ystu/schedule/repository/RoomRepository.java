package com.ystu.schedule.repository;

import com.ystu.schedule.entity.Room;
import com.ystu.schedule.entity.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByRoomType(RoomType roomType);

    Optional<Room> findByNumber(String number);

    boolean existsByNumber(String number);
}
