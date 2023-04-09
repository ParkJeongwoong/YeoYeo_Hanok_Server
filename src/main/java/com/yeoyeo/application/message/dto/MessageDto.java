package com.yeoyeo.application.message.dto;

import lombok.Getter;

@Getter
public class MessageDto {

    private final String to; // 수신번호
//    private String subject; // 개별 메시지 제목 (LMS, MMS만 사용가능)
//    private String content; // 개별 메시지 내용 (LMS, MMS만 사용가능)

    public MessageDto(String to) {
        this.to = to;
    }

}
