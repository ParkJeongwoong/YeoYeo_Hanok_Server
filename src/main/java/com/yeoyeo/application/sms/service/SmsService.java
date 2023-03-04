package com.yeoyeo.application.sms.service;

import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.sms.dto.SendMessageResponseDto;
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
public class SmsService {

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

    public SendMessageResponseDto test(String subject, String content, String to) {
        return sendSMS(subject, content, to);
    }

    public SendMessageResponseDto sendAuthenticationKeySms(String to) {
        // Todo - 휴대폰 번호 인증 문자
        String authKey = getAuthKey();
        String subject = "[한옥스테이 여여] 휴대폰 인증 문자입니다.";
        String content = "[한옥스테이 여여 문자 인증]\n\n" +
                "인증번호 : "+authKey+"\n" +
                "인증번호를 입력해 주세요.";
        registerAuthKey(to, authKey);
        return null;
    }

    public Boolean validateAuthenticationKey(String phoneNumber, String authKey) {
        return checkAuthKey(phoneNumber, authKey);
    }

    public SendMessageResponseDto sendReservationSms(Reservation reservation) {
        // Todo - 예약 완료 문자
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = reservation.getFirstDateRoom().getRoom().getName();
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 확정 안내 문자입니다.";
        String content = "[한옥스테이 여여 예약 확정 안내]\n\n" +
                "안녕하세요, 한옥스테이 여여입니다.\n" +
                "고객님의 "+startDate_string+" ~ "+endDate_string+room+" 예약이 확정되셨습니다.\n" +
                "(예약번호 :"+reservation.getId()+")\n\n" +
                "입실은 15시부터 이면 퇴실은 11시입니다.\n\n" +
                "한옥스테이 여여에서 여유롭고 행복한 시간 보내시길 바라겠습니다.\n" +
                "감사합니다.:)";
        return null;
    }

    public SendMessageResponseDto sendCancelSms(Reservation reservation) {
        // Todo - 예약 취소 문자
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = reservation.getFirstDateRoom().getRoom().getName();
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 취소 문자입니다.";
        String content = "[한옥스테이 여여 예약 취소 안내]\n\n" +
                "안녕하세요, 한옥스테이 여여입니다.\n" +
                "고객님의 "+startDate_string+" ~ "+endDate_string+room+" 예약이 정상적으로 취소되셨습니다.\n" +
                "(예약번호 :"+reservation.getId()+")\n" +
                "결제하신 내역은 환불 규정에 따라 진행될 예정입니다.\n\n" +
                "감사합니다.";
        return null;
    }

    public SendMessageResponseDto sendAdminSms(String message) {
        // Todo - 관리자 알림 문자
        String subject = "[한옥스테이 여여] 관리자 알림 문자입니다.";
        String content = "[한옥스테이 여여 관리자 알림 문자]\n\n" +
                "관리자 알림 문자입니다.\n" +
                "내용 : [\n" +
                message +
                "]\n" +
                "서버 데이터를 확인 바랍니다.";
        return sendMultipleSMS(subject, content, ADMIN_LIST);
    }

    private SendMessageResponseDto sendSMS(String subject, String content, String to) {
        String uri = "/sms/v2/services/"+smsKey+"/messages";
        String url = NCLOUD_SMS_URL+uri;
        String timestamp = getTimestamp();
        String signature = getSignature("POST", uri, timestamp);

        return webClientService.sendSms(url, subject, content, getNumberOnly(to), timestamp, accessKey, signature);
    }

    private SendMessageResponseDto sendMultipleSMS(String subject, String content, List<String> phoneNumberList) {
        String uri = "/sms/v2/services/"+smsKey+"/messages";
        String url = NCLOUD_SMS_URL+uri;
        String timestamp = getTimestamp();
        String signature = getSignature("POST", uri, timestamp);

        return webClientService.sendMultipleSms(url, subject, content, phoneNumberList.stream().map(this::getNumberOnly).collect(Collectors.toList()), timestamp, accessKey, signature);
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
