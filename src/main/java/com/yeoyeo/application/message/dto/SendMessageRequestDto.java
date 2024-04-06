package com.yeoyeo.application.message.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class SendMessageRequestDto {

    private final String type; // SMS | LMS | MMS
    private final String contentType; // COMM : 일반메시지 | AD : 광고 메시지
    private final String countryCode; // 국가번호
    private final String from = "01089599091"; // 발신번호
    private final String subject; // 메시지 제목
    private final String content; // 메시지 내용
    private final List<MessageDto> messages; // 메시지 정보 (개별 메시지 정보입력)
//    private FileDto files; // MMS만 사용 가능
//    private String reserveTime; // 예약 시간 (yyyy-MM-dd HH:mm)
//    private String reserveTimeZone; // 예약 시간 타임존 (Asia/Seoul)
//    private String scheduleCode; // 스케즐 코드 ("every-pm-16")

    public SendMessageRequestDto(String type, String subject, String content, String to) {
        this.type = type;
        this.contentType = "COMM";
        this.countryCode = "82";
        this.subject = subject;
        this.content = content;
        this.messages = new ArrayList<>();
        this.messages.add(new MessageDto(to));
    }

    public SendMessageRequestDto(String type, String subject, String content, List<String> phoneNumberList) {
        this.type = type;
        this.contentType = "COMM";
        this.countryCode = "82";
        this.subject = subject;
        this.content = content;
        this.messages = new ArrayList<>();
        phoneNumberList.forEach(phoneNumber -> this.messages.add(new MessageDto(phoneNumber)));
    }

}
