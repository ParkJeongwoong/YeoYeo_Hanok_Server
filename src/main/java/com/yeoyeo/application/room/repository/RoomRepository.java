package com.yeoyeo.application.room.repository;

import com.yeoyeo.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
