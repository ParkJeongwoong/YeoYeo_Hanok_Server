package com.yeoyeo.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String buyer_name;

    @Column(nullable = false)
    private String buyer_tel;

    @Column
    private String buyer_email;

    @Column
    private String buyer_addr;

    @Column(nullable = false)
    private String imp_uid;

    @Column(nullable = false)
    private String pay_method;

    @Column(nullable = false)
    private String receipt_url;

    @Column(nullable = false)
    private String status;

    @OneToOne(mappedBy = "payment")
    private Reservation reservation;

    @Builder
    public Payment(Integer amount, String buyer_name, String buyer_tel, String buyer_email, String buyer_addr,
                   String imp_uid, String pay_method, String receipt_url, String status) {
        this.amount = amount;
        this.buyer_name = buyer_name;
        this.buyer_tel = buyer_tel;
        this.buyer_email = buyer_email;
        this.buyer_addr = buyer_addr;
        this.imp_uid = imp_uid;
        this.pay_method = pay_method;
        this.receipt_url = receipt_url;
        this.status = status;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}
