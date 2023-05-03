package com.yeoyeo.application.message.service;

import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.message.dto.SendMessageResponseDto;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

    static final String NCLOUD_SMS_URL = "https://sens.apigw.ntruss.com";
    static final List<String> ADMIN_LIST = Arrays.asList("01020339091", "01089599091", "01026669091", "01020199091");

    @Value("${sms.ncloud.key}")
    String smsKey;
    @Value("${sms.ncloud.accessKey}")
    String accessKey;
    @Value("${sms.ncloud.secretKey}")
    String secretKey;

    @Autowired
    private final RedisTemplate redisTemplate;

    private final WebClientService webClientService;

    // SMS
    public SendMessageResponseDto sendAuthenticationKeyMsg(String to) {
        String authKey = getAuthKey();
        String subject = "[한옥스테이 여여] 휴대폰 인증 문자입니다.";
        String content = "[한옥스테이 여여 문자 인증]\n\n" +
                "인증번호 : "+authKey+"\n" +
                "인증번호를 입력해 주세요.";
        registerAuthKey(to, authKey);
        return sendMessage("SMS", subject, content, to);
    }

    public Boolean validateAuthenticationKey(String phoneNumber, String authKey) {
        return checkAuthKey(phoneNumber, authKey);
    }

    // LMS
    public SendMessageResponseDto sendReservationMsg(Reservation reservation) {
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = reservation.getRoom().getName();
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 확정 안내 문자입니다.";
        String content = "[한옥스테이 여여 예약 확정 안내]\n\n" +
                "안녕하세요, 한옥스테이 여여입니다.\n" +
                "고객님의 "+startDate_string+" ~ "+endDate_string+room+" 예약이 확정되셨습니다.\n" +
                "(예약번호 :"+reservation.getId()+")\n\n" +
                "입실은 15시부터 이며 퇴실은 11시입니다.\n\n" +
                "한옥스테이 여여에서 여유롭고 행복한 시간 보내시길 바라겠습니다.\n" +
                "감사합니다.:)";
        SendMessageResponseDto response = sendMessage("LMS", subject, content, to);

        String subject4Admin = "[한옥스테이 여여] 예약 확정 알림";
        String content4Admin = "[한옥스테이 여여 - 예약 확정 알림]\n\n" +
                "새로운 예약이 \"확정\" 되었습니다.\n" +
                "예약번호 : " + reservation.getId() + "\n" +
                "예약 날짜 : " + startDate_string+" ~ "+endDate_string+room + "\n" +
                "고객명 : " + reservation.getGuest().getName() + "\n" +
                "연락처 : " + reservation.getGuest().getPhoneNumber() + "\n\n";
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
        return response;
    }

    // LMS
    public SendMessageResponseDto sendCancelMsg(Reservation reservation) {
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = reservation.getRoom().getName();
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 취소 문자입니다.";
        String content = "[한옥스테이 여여 예약 취소 안내]\n\n" +
                "안녕하세요, 한옥스테이 여여입니다.\n" +
                "고객님의 "+startDate_string+" ~ "+endDate_string+room+" 예약이 정상적으로 취소되셨습니다.\n" +
                "(예약번호 :"+reservation.getId()+")\n" +
                "결제하신 내역은 환불 규정에 따라 진행될 예정입니다.\n\n" +
                "감사합니다.";
        SendMessageResponseDto response = sendMessage("LMS", subject, content, to);

        String subject4Admin = "[한옥스테이 여여] 예약 취소 알림";
        String content4Admin = "[한옥스테이 여여 - 예약 취소 알림]\n\n" +
                "홈페이지 예약이 \"취소\" 되었습니다.\n" +
                "예약번호 : " + reservation.getId() + "\n" +
                "예약 날짜 : " + startDate_string+" ~ "+endDate_string+room + "\n" +
                "고객명 : " + reservation.getGuest().getName() + "\n";
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
        return response;
    }

    // LMS
    public SendMessageResponseDto sendAdminMsg(String message) {
        String subject = "[한옥스테이 여여] 관리자 알림 문자입니다.";
        String content = "[한옥스테이 여여 관리자 알림 문자]\n\n" +
                "관리자 알림 문자입니다.\n" +
                "내용 : " + message;
        return sendMultipleMessage("LMS", subject, content, ADMIN_LIST);
    }

    // LMS
    public void sendCollisionMsg(Reservation reservation) {
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = reservation.getRoom().getName();
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 죄송합니다.";
        String content = "[한옥스테이 여여 예약 취소 안내]\n\n" +
                "안녕하세요, 한옥스테이 여여입니다.\n\n" +
                "먼저 죄송하다는 말씀을 드립니다.\n" +
                "현재 홈페이지의 사정으로 인해 고객님의 "+startDate_string+" ~ "+endDate_string+room+" 예약이 취소되셨습니다.\n" +
                "(예약번호 :"+reservation.getId()+")\n\n" +
                "한옥스테이 여여를 찾아 주신 것에 대한 고마움과 죄송한 마음을 담아 현재 연락처로 소정의 선물을 발송해 드리려고 합니다.\n" +
                "기대하셨을 여행에 실망을 안겨드려 다시 한 번 죄송합니다." +
                "결제하신 내역은 즉시 전액 환불될 예정이며 예약 취소에 대한 보상 역시 최대한 빠르게 전달해드리겠습니다.\n\n" +
                "감사합니다.";
        sendMessage("LMS", subject, content, to);

        String subject4Admin = "[한옥스테이 여여] 플랫폼 간 예약 중복으로 인한 예약 취소 발생";
        String content4Admin = "[한옥스테이 여여 관리자 알림 문자]\n\n" +
                "예약 정보 동기화 중 플랫폼 간 예약 중복이 발견되어 예약 취소가 발생되었습니다.\n\n" +
                "취소된 예약 번호는 "+reservation.getId()+" 이며 "+reservation.getGuest().getName()+" 고객의 연락처는 "+reservation.getGuest().getPhoneNumber()+" 입니다.\n\n" +
                "빠른 보상 전달 부탁드립니다.";
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
    }

    private SendMessageResponseDto sendMessage(String type, String subject, String content, String to) {
        String uri = "/sms/v2/services/"+smsKey+"/messages";
        String url = NCLOUD_SMS_URL+uri;
        String timestamp = getTimestamp();
        String signature = getSignature("POST", uri, timestamp);

        return webClientService.sendMessage(type, url, subject, content, getNumberOnly(to), timestamp, accessKey, signature);
    }

    private SendMessageResponseDto sendMultipleMessage(String type, String subject, String content, List<String> phoneNumberList) {
        String uri = "/sms/v2/services/"+smsKey+"/messages";
        String url = NCLOUD_SMS_URL+uri;
        String timestamp = getTimestamp();
        String signature = getSignature("POST", uri, timestamp);

        return webClientService.sendMultipleMessage(type, url, subject, content, phoneNumberList.stream().map(this::getNumberOnly).collect(Collectors.toList()), timestamp, accessKey, signature);
    }

    private String getSignature(String method, String uri, String timestamp) {
        String space = " ";
        String newLine = "\n";

        String message = new StringBuilder()
                .append(method).append(space)
                .append(uri).append(newLine)
                .append(timestamp).append(newLine)
                .append(accessKey).toString();

        String encodeBase64String;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            log.error("Signature 생성 중 NoSuchAlgorithmException 에러 발생", e);
            encodeBase64String = e.toString();
        } catch (InvalidKeyException e) {
            log.error("Signature 생성 중 InvalidKeyException 에러 발생", e);
            encodeBase64String = e.toString();
        }
        return encodeBase64String;
    }

    private String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String getAuthKey() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void registerAuthKey(String phoneNumber, String authKey) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(phoneNumber, authKey, 3, TimeUnit.MINUTES);
    }

    private boolean checkAuthKey(String phoneNumber, String authKey) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String redisAuthKey = valueOperations.get(phoneNumber);
        if (redisAuthKey == null) return false;
        if (valueOperations.get(phoneNumber).equals(authKey)) {
            return redisTemplate.delete(phoneNumber);
        }
        return false;
    }

    private String getNumberOnly(String string) {
        return string.replaceAll("[^0-9]","");
    }

}
