package com.yeoyeo.application.sms.service;

import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.sms.dto.SendMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {

    static final String NCLOUD_SMS_URL = "https://sens.apigw.ntruss.com";

    @Value("${sms.ncloud.key}")
    String smsKey;
    @Value("${sms.ncloud.accessKey}")
    String accessKey;
    @Value("${sms.ncloud.secretKey}")
    String secretKey;

    private final WebClientService webClientService;

    public SendMessageResponseDto test(String subject, String content, String to) {
        return sendSMS(subject, content, to);
    }

    public SendMessageResponseDto sendCertificationSms(String to) {
        // Todo - 휴대폰 번호 인증 문자
        return null;
    }

    public SendMessageResponseDto sendReservationSms(String to) {
        // Todo - 예약 완료 문자
        return null;
    }

    public SendMessageResponseDto sendCancelSms(String to) {
        // Todo - 예약 취소 문자
        return null;
    }

    private SendMessageResponseDto sendSMS(String subject, String content, String to) {
        String uri = "/sms/v2/services/"+smsKey+"/messages";
        String url = NCLOUD_SMS_URL+uri;
        String timestamp = getTimestamp();
        String signature = getSignature("POST", uri, timestamp);

        return webClientService.sendSms(url, subject, content, to, timestamp, accessKey, signature);
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

}
