package com.yeoyeo.application.sms.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SendMessageRequestDto {

    private final String type; // SMS | LMS | MMS -> 여기선 SMS
    private final String contentType; // COMM : 일반메시지 | AD : 광고 메시지
    private final String countryCode; // 국가번호
    private final String from; // 발신번호 - Todo : 법인 계정 등록 후 발신번호 생성 필요
    private final String subject; // 메시지 제목
    private final String content; // 메시지 내용
    private final List<MessageDto> messages; // 메시지 정보 (개별 메시지 정보입력)
//    private FileDto files; // MMS만 사용 가능
//    private String reserveTime; // 예약 시간 (yyyy-MM-dd HH:mm)
//    private String reserveTimeZone; // 예약 시간 타임존 (Asia/Seoul)
//    private String scheduleCode; // 스케즐 코드 ("every-pm-16")

    public SendMessageRequestDto(String subject, String content, String to) {
        this.type = "SMS";
        this.contentType = "COMM";
        this.countryCode = "82";
        this.from = "01020339091"; // Todo : 법인 계정 생성 후 수정 필요
        this.subject = subject;
        this.content = content;
        this.messages = new ArrayList<>();
        this.messages.add(new MessageDto(to));
    }

    public SendMessageRequestDto(String subject, String content, List<String> phoneNumberList) {
        this.type = "SMS";
        this.contentType = "COMM";
        this.countryCode = "82";
        this.from = "01020339091"; // Todo : 법인 계정 생성 후 수정 필요
        this.subject = subject;
        this.content = content;
        this.messages = new ArrayList<>();
        phoneNumberList.forEach(phoneNumber -> this.messages.add(new MessageDto(phoneNumber)));
    }

}
