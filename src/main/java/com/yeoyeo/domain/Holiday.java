package com.yeoyeo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
public class Holiday {

    @Id
    private LocalDate holiday_date;
    @Column
    private String holiday_name;
    @Column
    private Integer holiday_year;
    @Column
    private Integer holiday_month;
    @Column
    private Integer holiday_day;

    @Builder
    public Holiday(LocalDate date, String name) {
        this.holiday_date = date;
        this.holiday_name = name;
        this.holiday_year = date.getYear();
        this.holiday_month = date.getMonthValue();
        this.holiday_day = date.getDayOfMonth();
    }

}
