package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
public class Holiday {

    @Id
    private LocalDate date;
    @Column
    private String name;
    @Column
    private Integer year;
    @Column
    private Integer month;
    @Column
    private Integer day;

    @Builder
    public Holiday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
        this.year = date.getYear();
        this.month = date.getMonthValue();
        this.day = date.getDayOfMonth();
    }

}
