package com.yeoyeo.domain.Admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Timestamp;

@Entity
public class PersistentLogins {
    @Id
    private String series;
    @Column(nullable = false, length = 100)
    private String username;
    @Column(nullable = false, length = 64)
    private String token;
    @Column(nullable = false, length = 64)
    private Timestamp last_used;
}
