package com.yeoyeo.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

//@SuperBuilder
@Getter
//@MappedSuperclass
@NoArgsConstructor
@Entity
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(length = 30, nullable = false)
    protected String name;

//    @Builder
    public Guest(String name) {
        this.name = name;
    }
}