package com.yeoyeo.domain.Admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
