package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.dateroom.dto.*;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("dateroom")
public class DateRoomController {

    private final DateRoomService dateRoomService;

    @GetMapping("show-all")
    public List<DateRoomInfoDto> showAllDateRooms() {
        return dateRoomService.showAllDateRooms();
    }

    @GetMapping("list/{year}/{month}")
    public List<DateRoomInfoDto> show2MonthsDateRooms(@PathVariable("year") int year, @PathVariable("month") int month) {
        return dateRoomService.show2MonthsDateRooms(year, month);
    }
}
