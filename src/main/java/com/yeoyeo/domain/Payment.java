package com.yeoyeo.domain;

import com.yeoyeo.application.payment.etc.exception.PaymentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Getter
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

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column
    private Integer canceled_amount;

    @Column
    private String cancel_reason;

    @Column
    private String cancel_receipt_url;

    @Builder
    public Payment(Integer amount, String buyer_name, String buyer_tel, String buyer_email, String buyer_addr,
                   String imp_uid, String pay_method, String receipt_url, String status, Reservation reservation) {
        this.amount = amount;
        this.buyer_name = buyer_name;
        this.buyer_tel = buyer_tel;
        this.buyer_email = buyer_email;
        this.buyer_addr = buyer_addr;
        this.imp_uid = imp_uid;
        this.pay_method = pay_method;
        this.receipt_url = receipt_url;
        this.status = status;
        this.reservation = reservation;
        this.canceled_amount = 0;
    }

    public Payment(Integer amount, String buyer_name, String buyer_tel, String buyer_email, String buyer_addr,
                   String imp_uid, String pay_method, String receipt_url, String status, Reservation reservation,
                   Integer canceled_amount, String cancel_reason, String cancel_receipt_url) {
        this.amount = amount;
        this.buyer_name = buyer_name;
        this.buyer_tel = buyer_tel;
        this.buyer_email = buyer_email;
        this.buyer_addr = buyer_addr;
        this.imp_uid = imp_uid;
        this.pay_method = pay_method;
        this.receipt_url = receipt_url;
        this.status = status;
        this.reservation = reservation;
        this.canceled_amount = canceled_amount;
        this.cancel_reason = cancel_reason;
        this.cancel_receipt_url = cancel_receipt_url;
    }

    public Integer getCancelableAmount() throws PaymentException {
        int cancelableAmount = this.amount - this.canceled_amount;
        if (cancelableAmount <= 0) throw new PaymentException("전액환불된 결제입니다.");
        return cancelableAmount;
    }

    public void setCanceled(long canceled_amount, String cancel_reason, String cancel_receipt_url) {
        this.canceled_amount = (int) canceled_amount;
        this.cancel_reason = cancel_reason;
        this.cancel_receipt_url = cancel_receipt_url;
        this.status = "cancelled";
    }

    public Payment clone() {
        return new Payment(this.amount, this.buyer_name, this.buyer_tel, this.buyer_email, this.buyer_addr,
                           this.imp_uid, this.pay_method, this.receipt_url, this.status, this.reservation,
                           this.canceled_amount, this.cancel_reason, this.cancel_receipt_url);
    }
}
