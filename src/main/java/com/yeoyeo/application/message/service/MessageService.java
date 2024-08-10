package com.yeoyeo.application.message.service;

import com.yeoyeo.application.common.method.CommonMethod;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.message.dto.SendMessageResponseDto;
import com.yeoyeo.application.reservation.dto.AuthKeyResponseDto;
import com.yeoyeo.application.reservation.dto.SendAdminCheckInMsgDto;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Admin.AdminManageInfo;
import com.yeoyeo.domain.Reservation;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

    static final String NCLOUD_SMS_URL = "https://sens.apigw.ntruss.com";
    static final List<String> ADMIN_LIST = Arrays.asList("01020339091", "01089599091", "01026669091", "01020199091");
    static final String HOST = "01089599091";
    static final String DEV = "01020339091";
    static final String CHANGE_OFFER_URL = "https://yeoyeo.co.kr/offer/";
    static final String TEMP_CHANGE_OFFER_URL = "https://api.yeoyeo.co.kr/reservation/offer/";

    @Value("${sms.ncloud.key}")
    String smsKey;
    @Value("${sms.ncloud.accessKey}")
    String accessKey;
    @Value("${sms.ncloud.secretKey}")
    String secretKey;
    @Value("${sms.redis.fallbackKey}")
    String fallbackKey;

    String changeLine = System.lineSeparator();
    String changeTwoLine = changeLine + changeLine;

    private final SecureRandom random = new SecureRandom();

    private final WebClientService webClientService;

    private final CommonMethod commonMethod;

    private final ReservationRepository reservationRepository;

    // SMS
    public AuthKeyResponseDto sendAuthenticationKeyMsg(String to) {
        String authKey = getAuthKey();
        String subject = "[한옥스테이 여여] 휴대폰 인증 문자입니다.";
        String content = "[한옥스테이 여여 문자 인증]" + changeTwoLine +
                String.format("인증번호 : %s", authKey) +
                System.lineSeparator() +
                "인증번호를 입력해 주세요.";
        String token = registerAuthKey(to, authKey);
        SendMessageResponseDto response = sendMessage("SMS", subject, content, to);
        return new AuthKeyResponseDto(response.getStatusCode(), token);
    }

    public Boolean validateAuthenticationKey(String phoneNumber, String authKey, String authToken) {
        return checkAuthKey(phoneNumber, authKey, authToken);
    }

    // LMS
    public void sendReservationMsg(Reservation reservation) {
        if (checkAdminName(reservation.getGuest().getName())) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return;
        }
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = String.format("%d년 %d월 %d일", startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth());
        String endDate_string = String.format("%d년 %d월 %d일", endDate.getYear(), endDate.getMonthValue(), endDate.getDayOfMonth());
        String room = String.format("[%s]", reservation.getRoom().getName());
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 확정 안내 문자입니다.";
        String content = "[한옥스테이 여여 예약 확정 안내]" + changeTwoLine +
                "안녕하세요, 한옥스테이 여여입니다." + changeLine +
                "고객님의 "+startDate_string+" ~ "+endDate_string+" "+room+" 예약이 확정되셨습니다." + changeLine +
                "(예약번호 :"+reservation.getId()+")" + changeTwoLine +
                "입실은 15시부터 이며 퇴실은 11시입니다." + changeTwoLine +
                "한옥스테이 여여에서 여유롭고 행복한 시간 보내시길 바라겠습니다." + changeLine +
                "감사합니다.:)" + changeTwoLine +
                "(추가적인 문의가 있으시면 " + HOST + " 로 연락 부탁드립니다.)";

        String subject4Admin = "[한옥스테이 여여] 예약 확정 알림";
        String content4Admin = "[한옥스테이 여여 - 예약 확정 알림]" + changeTwoLine +
                "새로운 예약이 \"확정\" 되었습니다." + changeLine +
                "예약번호 : " + reservation.getId() + changeLine +
                "예약 날짜 : " + startDate_string+" ~ "+endDate_string+" "+room + changeLine +
                "고객명 : " + reservation.getGuest().getName() + " (" + reservation.getGuest().getGuestCount() + "명)" + changeLine +
                "연락처 : " + reservation.getGuest().getPhoneNumber();

        log.info("관리자에게 문자를 보냅니다. to : {}", ADMIN_LIST);
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
        log.info("예약자에게 문자를 보냅니다. to : {}", to);
        SendMessageResponseDto responseDto = sendMessage("LMS", subject, content, to);
        log.info("예약 문자 전송 결과 : {} / {}", responseDto.getStatusCode(), responseDto.getStatusName());
    }

    // LMS
    public void sendCancelMsg(Reservation reservation) {
        if (checkAdminName(reservation.getGuest().getName())) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return;
        }
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = "["+reservation.getRoom().getName()+"]";
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 취소 문자입니다.";
        String content = "[한옥스테이 여여 예약 취소 안내]" + changeTwoLine +
                "안녕하세요, 한옥스테이 여여입니다." + changeLine +
                "고객님의 "+startDate_string+" ~ "+endDate_string+" "+room+" 예약이 정상적으로 취소되셨습니다." + changeLine +
                "(예약번호 :"+reservation.getId()+")" + changeLine +
                "결제하신 내역은 환불 규정에 따라 진행될 예정입니다." + changeTwoLine +
                "감사합니다.";

        String subject4Admin = "[한옥스테이 여여] 예약 취소 알림";
        String content4Admin = "[한옥스테이 여여 - 예약 취소 알림]" + changeTwoLine +
                "홈페이지 예약이 \"취소\" 되었습니다." + changeLine +
                "예약번호 : " + reservation.getId() + "" + changeLine +
                "예약 날짜 : " + startDate_string+" ~ "+endDate_string+" "+room + changeLine +
                "고객명 : " + reservation.getGuest().getName();

        log.info("관리자에게 문자를 보냅니다. to : {}", ADMIN_LIST);
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
        log.info("예약자에게 문자를 보냅니다. to : {}", to);
        SendMessageResponseDto responseDto = sendMessage("LMS", subject, content, to);
        log.info("취소 문자 전송 결과 : {} / {}", responseDto.getStatusCode(), responseDto.getStatusName());
    }

    // LMS
    public void sendAdminMsg(String message) {
        String subject = "[한옥스테이 여여] 관리자 알림 문자입니다.";
        String content = "[한옥스테이 여여 관리자 알림 문자]" + changeTwoLine +
                "관리자 알림 문자입니다." + changeLine +
                "내용 : " + message;
        SendMessageResponseDto responseDto = sendMultipleMessage("LMS", subject, content, ADMIN_LIST);
        log.info("예약 문자 전송 결과 : {} / {}", responseDto.getStatusCode(), responseDto.getStatusName());
    }

    // LMS
    public void sendCollisionMsg(Reservation reservation) {
        if (checkAdminName(reservation.getGuest().getName())) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return;
        }
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = "["+reservation.getRoom().getName()+"]";
        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 죄송합니다.";
        String content = "[한옥스테이 여여 예약 취소 안내]" + changeTwoLine +
                "안녕하세요, 한옥스테이 여여입니다." + changeTwoLine +
                "먼저 죄송하다는 말씀을 드립니다." + changeLine +
                "현재 홈페이지의 사정으로 인해 고객님의 "+startDate_string+" ~ "+endDate_string+" "+room+" 예약이 취소되셨습니다." + changeLine +
                "(예약번호 :"+reservation.getId()+")" + changeTwoLine +
//                "한옥스테이 여여를 찾아 주신 것에 대한 고마움과 죄송한 마음을 담아 현재 연락처로 소정의 선물을 발송해 드리려고 합니다." + changeLine +
//                "기대하셨을 여행에 실망을 안겨드려 다시 한 번 죄송합니다." +
//                "결제하신 내역은 즉시 전액 환불될 예정이며 예약 취소에 대한 보상 역시 최대한 빠르게 전달해드리겠습니다." + changeTwoLine +
                "결제하신 내역은 즉시 카드사에 전액 환불 요청될 예정이며 환불 요청 후 입금에 시간이 걸릴 수 있습니다." + changeLine +
                "기대하셨을 여행에 실망을 안겨드려 다시 한 번 죄송합니다." + changeLine +
                "감사합니다.";

        String subject4Admin = "[한옥스테이 여여] 플랫폼 간 예약 중복으로 인한 예약 취소 발생";
        String content4Admin = "[한옥스테이 여여 관리자 알림 문자]" + changeTwoLine +
                "예약 정보 동기화 중 플랫폼 간 예약 중복이 발견되어 예약 취소가 발생되었습니다." + changeLine +
                "취소된 예약은 "+startDate_string+" ~ "+endDate_string+" "+room+"예약(예약번호 :"+reservation.getId()+")이며 "+reservation.getGuest().getName()+" 고객의 연락처는 "+reservation.getGuest().getPhoneNumber()+" 입니다.";
//                "빠른 보상 전달 부탁드립니다.";

        log.info("예약자에게 문자를 보냅니다. to : {}", to);
        sendMessage("LMS", subject, content, to);
        log.info("관리자에게 문자를 보냅니다. to : {}", ADMIN_LIST);
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
    }

    // LMS
    public void sendNoticeMsgToConfirmedReservations(long roomId) {
        log.info("[동기화 후 확정 예약 건에 대한 안내 문자 전송]");
        List<Reservation> reservations = reservationRepository.findAllByReservationState(1)
                .stream().filter(reservation -> reservation.getManagementLevel() == 1 && reservation.getRoom().getId() == roomId && reservation.getGuest().getPhoneNumber() != null)
                .collect(Collectors.toList());
        log.info("문자 발송 대상 : {}건", reservations.size());
        reservations.forEach(reservation -> {
            log.info("SEND TO {} - {}", reservation.getId(), reservation.getGuest().getPhoneNumber());
            sendNoticeMsg(reservation.getGuest().getNumberOnlyPhoneNumber());
            reservation.setManagementLevel(2);
        });
        reservationRepository.saveAll(reservations);
    }

    // LMS
    public SendMessageResponseDto sendNoticeMsg(String numberOnlyPhoneNumber) {
        if (checkAdminPhone(numberOnlyPhoneNumber)) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return null;
        }
        String subject = "[한옥스테이 여여] 숙박 안내문자";
        String content = "안녕하세요 :)" + changeLine +
                "한옥스테이 여여 입니다." + changeLine +
                "여여를 방문하시기 전 보내드리는 안내 메시지입니다!"
                + changeTwoLine +
                "1. 입퇴실 안내" + changeLine +
                "[입퇴실은 셀프 체크인, 체크아웃]으로 진행되며 [체크아웃 시 문자로 확인] 부탁드립니다 :)" + changeLine +
                "[입실은 오후 3시] 부터 가능하시고 현관문 비밀번호는 [예약한 전화번호 뒤 4자리 + *] 입니다." + changeLine +
                "[퇴실 시간은 오전 11시]이오니 시간을 준수해 주시면 감사하겠습니다." +
                changeTwoLine +
                "2. 오시는 길" + changeLine +
                "한옥스테이 여여는 [경주시 갯마을길 53]에 위치하고 있으며 주차장이 있으니 차량을 이용하여 방문하시는 것을 추천 드립니다." +
                changeTwoLine +
                "3. 제공 서비스" + changeLine +
                "게스트를 위한 [물 2병, 소금빵, 캡슐커피, 와인잔, 와인오프너, 어메니티 (칫솔, 치약, 비누), 샴푸, 컨디셔너, 바디워시, 샤워타월, 수건, 드라이기, 충전기 및 여여에서 생활하는 동안 입으실 수 있는 생활 한복]이 준비되어 있습니다." + changeLine +
                "또 머무르는 동안 사용하실 수 있는 [빔프로젝터, 발뮤다 토스터, 네스프레소 커피머신, 전자레인지, 냉장고, 커피포트]가 있으니 편히 이용하시길 바랍니다." +
                changeTwoLine +
                "4. 안내사항" + changeLine +
                "- [반려동물 입실은 불가]한 점 참고 바랍니다." + changeLine +
                "- 주변에 산이 인접해 있기 때문에 마당과 실내 및 주변 공간은 모두 금연구역인 점 양해부탁드립니다." + changeLine +
                "- 목조건물이기 때문에 [화기 사용이 불가능]하며 요리도 불가능한 점 양해 부탁드립니다." + changeLine +
                "- 숙소 내 시설 및 침구류 등에 지울 수 없는 오염이 발생하는 경우 청소비 및 교체 비용이 발생할 수 있으니 배려 부탁드립니다." + changeLine +
                "- 입욕제를 사용할 수 없는 점 양해 부탁드립니다. 착색/배수구 막힘 발생 시 예약을 받지 못하게 되므로 예약비와 수리비가 청구됩니다. (물에 녹는 입욕솔트는 사용 가능합니다)" +
                changeTwoLine +
                "여여에서의 시간이 편안한 휴식이 되시길 바랍니다." + changeLine +
                "감사합니다." + changeTwoLine +
                "(추가적인 문의가 있으시면 " + HOST + " 로 연락 부탁드립니다.)";

        log.info("문자 발송 대상 : {}", numberOnlyPhoneNumber);
        return sendMessage("LMS", subject, content, numberOnlyPhoneNumber);
    }

    // LMS
    public SendMessageResponseDto sendNotice7DaysBeforeMsg(String numberOnlyPhoneNumber) {
        if (checkAdminPhone(numberOnlyPhoneNumber)) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return null;
        }
        String subject = "[한옥스테이 여여] 숙박 안내문자";
        String content = "안녕하세요 :)" + changeLine +
            "한옥스테이 여여 입니다." + changeLine +
            "방문하시기 일주일 전, 여행을 준비할 수 있도록 보내드리는 안내 메시지입니다." +
            changeTwoLine +
            "1. 입퇴실 안내" + changeLine +
            "[입퇴실은 셀프 체크인, 체크아웃]으로 진행되며 [입실 시간은 오후 3시, 퇴실 시간은 오전 11시]입니다." +
            changeTwoLine +
            "2. 오시는 길" + changeLine +
            "한옥스테이 여여는 [경주시 갯마을길 53]에 위치하고 있으며 주차장이 있으니 차량을 이용하여 방문하시는 것을 추천 드립니다." + changeLine +
            "자세한 위치는 네이버 지도, 카카오 지도, 티맵 등에서 [한옥스테이 여여]를 검색하셔서도 찾으실 수 있습니다." +
            changeTwoLine +
            "3. 제공 서비스" + changeLine +
            "게스트를 위한 [물 2병, 소금빵, 캡슐커피, 와인잔, 와인오프너, 어메니티 (칫솔, 치약, 비누), 샴푸, 컨디셔너, 바디워시, 샤워타월, 수건, 드라이기, 충전기 및 여여에서 생활하는 동안 입으실 수 있는 생활 한복]이 준비되어 있습니다." + changeLine +
            "또 머무르는 동안 사용하실 수 있는 [빔프로젝터, 발뮤다 토스터, 네스프레소 커피머신, 전자레인지, 냉장고, 커피포트]가 있으니 편히 이용하시길 바랍니다." +
            changeTwoLine +
            "4. 안내사항" + changeLine +
            "- [반려동물 입실은 불가]한 점 참고 바랍니다." + changeLine +
            "- 주변에 산이 인접해 있기 때문에 마당과 실내 및 주변 공간은 모두 금연구역인 점 양해부탁드립니다." + changeLine +
            "- 목조건물이기 때문에 [화기 사용이 불가능]하며 요리도 불가능한 점 양해 부탁드립니다." + changeLine +
            "- 숙소 내 시설 및 침구류 등에 지울 수 없는 오염이 발생하는 경우 청소비 및 교체 비용이 발생할 수 있으니 배려 부탁드립니다." + changeLine +
            "- 입욕제를 사용할 수 없는 점 양해 부탁드립니다. 착색/배수구 막힘 발생 시 예약을 받지 못하게 되므로 예약비와 수리비가 청구됩니다. (물에 녹는 입욕솔트는 사용 가능합니다)" +
            changeTwoLine +
            "그럼 일주일 뒤 뵙겠습니다." + changeLine +
            "감사합니다." + changeTwoLine +
            "(추가적인 문의가 있으시면 " + HOST + " 로 연락 부탁드립니다.)";

        log.info("문자 발송 대상 : {}", numberOnlyPhoneNumber);
        return sendMessage("LMS", subject, content, numberOnlyPhoneNumber);
    }

    // LMS
    public SendMessageResponseDto sendCheckInMsg(String numberOnlyPhoneNumber, String room) {
        if (checkAdminPhone(numberOnlyPhoneNumber)) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return null;
        }
        String subject = "[한옥스테이 여여] 체크인 안내문자";
        String content = "안녕하세요 :)" + changeLine +
                "한옥스테이 여여 입니다." + changeLine +
                "여여에 머무르시는 동안 필요한 내용들을 안내해드리겠습니다." +
                changeTwoLine +
                "[고객님이 머무실 방은 " + room + "입니다.]" +
                changeTwoLine +
                "<입퇴실 안내>" + changeLine +
                "[입퇴실은 셀프 체크인, 체크아웃]으로 진행되며 [체크아웃 시 문자로 확인] 부탁드립니다 :)" + changeLine +
                "[입실은 오후 3시] 부터 가능하시고 현관문 비밀번호는 [예약한 전화번호 뒤 4자리 + *] 입니다." + changeLine +
                "[퇴실 시간은 오전 11시]이오니 시간을 준수해 주시면 감사하겠습니다." +
                changeTwoLine +
                "<오시는 길>" + changeLine +
                "한옥스테이 여여는 [경주시 갯마을길 53]에 위치하고 있으며 주차장이 있으니 차량을 이용하여 방문하시는 것을 추천 드립니다." + changeLine +
                "주차는 객실 이름이 적힌 구역에 하시면 되십니다." +
                changeTwoLine +
                "<와이파이 비밀번호>" + changeLine +
                "와이파이 비밀번호는 [ yeoyeo9091 ] 입니다." +
                changeTwoLine +
//                "<빔프로젝터 사용법>" + changeLine +
//                "빔 프로젝터용 리모컨은 3개 입니다." + changeLine +
//                "- 큰 흰색 리모컨 : 빔프로젝터 리모컨" + changeLine +
//                "- 작은 흰색 리모컨 : 구글 크롬캐스트 리모컨" + changeLine +
//                "- 검은색 리모컨 : 스피커 리모컨" + changeLine +
//                "1. 큰 흰색 리모컨으로 빔프로젝터 전원을 켭니다." + changeLine +
//                "2. 작은 흰색 리모컨으로 구글 크롬캐스트 전원을 켭니다." + changeLine +
//                "3. 작은 흰색 리모컨을 조작해서 유튜브 / 넷플릭스를 시청할 수 있습니다." + changeLine +
//                "4. 소리는 검은색 리모컨으로 조절할 수 있습니다. (스피커를 향한 상태로 조절해 주세요)" +
//                changeTwoLine +
//                "<발뮤다 토스터 사용법>" + changeLine +
//                "발뮤다 토스터 사용법은 다음과 같습니다." + changeLine +
//                "1. 상단의 뚜껑을 열고 전용컵으로 물 5cc를 넣어줍니다." + changeLine +
//                "2. 토스터에 냉동 소금빵을 넣고 전원을 켭니다." + changeLine +
//                "3. 크루아상 모드로 설정하고 시간은 6분으로 설정합니다." + changeLine +
//                "(더 따뜻하게 먹고 싶으신 분들은 물 5cc를 다시 넣고 1분 추가로 돌리시면 됩니다)" +
//                changeTwoLine +
//                "<네스프레소 커피머신 사용법>" + changeLine +
//                "네스프레소 커피머신 사용법은 다음과 같습니다." + changeLine +
//                "1. 커피머신 뒤 쪽 물탱크에 물 넣어주기" + changeLine +
//                "2. 커피머신 전원 켜기 (30초 정도 불이 깜빡이며 예열하는 시간이 있습니다)" + changeLine +
//                "3. 상단의 뚜껑을 열고 네스프레소 캡슐 넣기" + changeLine +
//                "4. 뚜껑을 닫고 커피 추출구 앞에 컵 놓기" + changeLine +
//                "5. 에스프레소(40ml) / 룽고(110ml) 버튼 눌러 커피 추출하기" +
//                changeTwoLine +
                "<안내사항>" + changeLine +
                "- 주변에 산이 인접해 있기 때문에 마당과 실내 및 주변 공간은 모두 금연구역인 점 양해부탁드립니다." + changeLine +
                "- 목조건물이기 때문에 화기 사용이 불가능하며 요리도 불가능한 점 양해 부탁드립니다." + changeLine +
                "- 숙소 내 시설 및 침구류 등에 지울 수 없는 오염이 발생하는 경우 청소비 및 교체 비용이 발생할 수 있으니 배려 부탁드립니다." + changeLine +
                "- 입욕제는 사용이 불가능합니다. 입욕제로 인해 착색/배수구 막힘 발생 시 예약을 받지 못하게 되므로 예약비와 수리비가 청구됩니다. (물에 녹는 입욕솔트는 가능합니다)" + changeLine +
                "- 빔프로젝터, 발뮤다 토스터, 네스프레소 커피머신 등의 가전제품은 객실 내 비치된 여여 사용설명서를 참고 부탁드립니다." + changeLine +
                changeTwoLine +
                "편안하고 즐거운 시간 보내시길 바랍니다." + changeLine +
                "감사합니다!" + changeTwoLine +
                "(추가적인 문의가 있으시면 " + HOST + " 로 연락 부탁드립니다.)";

        log.info("문자 발송 대상 : {}", numberOnlyPhoneNumber);
        return sendMessage("LMS", subject, content, numberOnlyPhoneNumber);
    }

    // LMS
    public void sendAfterCheckInMsg(String numberOnlyPhoneNumber) {
        if (checkAdminPhone(numberOnlyPhoneNumber)) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return;
        }
        String subject = "[한옥스테이 여여] 안내문자";
        String content = "안녕하세요 :)" + changeLine +
                "한옥스테이 여여 입니다." + changeLine +
                "3시에 숙소 상태 최고의 컨디션 확인하였으나 체크인 하실 때. 바닥이나 방석에 작은 나무나 흙알갱이들이 떨어져 있을 수 있어요. 전통방식의 한옥의 특징이니  양해 부탁 드립니다^^" + changeLine +
                "뒷뜰 바구니에 먼지털이와 물티슈를 넣어두었어요. 필요하실 때 사용하시길 바랍니다.^^" + changeLine +
                "감사합니다.";

        log.info("문자 발송 대상 : {}", numberOnlyPhoneNumber);
        SendMessageResponseDto responseDto = sendMessage("LMS", subject, content, numberOnlyPhoneNumber);
        if (responseDto != null) {
            log.info("문자 발송 결과 : {} / {}", responseDto.getStatusCode(), responseDto.getStatusName());
        }
    }

    // LMS
    public void sendAdminCheckInMsg(SendAdminCheckInMsgDto msgDto) {
        String subject = "[한옥스테이 여여] 체크인 알림";
        List<AdminManageInfo> guestInfoList = msgDto.getGuestInfos().stream().filter(guestInfo -> !checkAdminName(guestInfo.getName())).collect(Collectors.toList());
        if (guestInfoList.isEmpty()) {
            log.info("체크인 정보가 없습니다.");
            return;
        }
        LocalDate checkInDate = guestInfoList.get(0).getCheckin();
        String roomA_guestName; // 여유
        String roomA_guestPhone;
        int roomA_guestCount;
        String roomA_string = null;
        String roomB_guestName; // 여행
        String roomB_guestPhone;
        int roomB_guestCount;
        String roomB_string = null;
        boolean airbnbRouting = false;
        int roomA_night;
        int roomB_night;

        for (AdminManageInfo guestInfo : guestInfoList) {
            if (guestInfo.getRoom().getId() == 1) {
                roomA_guestName = guestInfo.getName();
                roomA_guestPhone = guestInfo.getPhoneNumber();
                roomA_guestCount = guestInfo.getGuestCount();
                roomA_night = guestInfo.getNight();
                roomA_string = changeLine + "여유 : " + roomA_guestName + " / " + roomA_guestPhone + " / " + roomA_guestCount + "명" + " / " + roomA_night + "박";
                if (roomA_guestName.equals("AirBnbGuest")) airbnbRouting = true;
            } else if (guestInfo.getRoom().getId() == 2) {
                roomB_guestName = guestInfo.getName();
                roomB_guestPhone = guestInfo.getPhoneNumber();
                roomB_guestCount = guestInfo.getGuestCount();
                roomB_night = guestInfo.getNight();
                roomB_string = changeLine + "여행 : " + roomB_guestName + " / " + roomB_guestPhone + " / " + roomB_guestCount + "명" + " / " + roomB_night + "박";
                if (roomB_guestName.equals("AirBnbGuest")) airbnbRouting = true;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(checkInDate).append(" 체크인 숫자 : ").append(guestInfoList.size()).append("팀");
        if (roomA_string != null) stringBuilder.append(roomA_string);
        if (roomB_string != null) stringBuilder.append(roomB_string);
        if (airbnbRouting) {
            stringBuilder
                .append(changeTwoLine)
                .append("에어비앤비를 통해 예약을 확인하세요. (https://www.airbnb.co.kr/login?redirect_url=%2Fhosting%2Freservations)");
        }
        String content = stringBuilder.toString();

        SendMessageResponseDto responseDto = sendMessage("LMS", subject, content, getNumberOnly(HOST));
        if (responseDto != null) {
            log.info("문자 발송 결과 : {} / {}", responseDto.getStatusCode(), responseDto.getStatusName());
        }
    }

    public void sendDevMsg(String msg) {
        sendMessage("SMS", "[한옥스테이 여여] 개발자 문자", msg, getNumberOnly(DEV));
    }

    public void sendChangeOfferMsg(Reservation reservation) {
        if (checkAdminName(reservation.getGuest().getName())) {
            log.info("예약자 이름이 관리자 이름과 동일합니다. 예약 문자를 보내지 않습니다.");
            return;
        }
        LocalDate startDate = reservation.getFirstDateRoom().getDate();
        LocalDate endDate = reservation.getLastDateRoom().getDate().plusDays(1);
        String startDate_string = startDate.getYear()+"년 "+startDate.getMonthValue()+"월"+startDate.getDayOfMonth()+"일";
        String endDate_string = endDate.getYear()+"년 "+endDate.getMonthValue()+"월"+endDate.getDayOfMonth()+"일 ";
        String room = "["+reservation.getRoom().getName()+"]";
        String anotherRoom = reservation.getRoom().getName().equals("여유") ? "[여행]" : "[여유]";

        String to = reservation.getGuest().getNumberOnlyPhoneNumber();
        String subject = "[한옥스테이 여여] 예약 변경 제안";
        String content = "[한옥스테이 여여 예약 변경 제안]" + changeTwoLine +
            "안녕하세요, 한옥스테이 여여입니다." + changeTwoLine +
            "먼저 죄송하다는 말씀을 드립니다." + changeLine +
            "현재 숙소의 사정으로 인해 고객님의 "+startDate_string+" ~ "+endDate_string+" "+room+" 예약이 어려운 상황입니다." + changeLine +
            "대신 동일한 일정으로 "+startDate_string+" ~ "+endDate_string+" "+anotherRoom+"방의 예약은 가능한 상황입니다." + changeLine +
            "기존에 예약하신 방과 크기는 동일하고 구조만 조금 차이가 있는 또 다른 매력을 가진 방입니다." + changeLine +
            "예약 변경을 원하신다면 다음의 링크를 통해 변경을 신청해 주세요." + changeLine +
            "( " + TEMP_CHANGE_OFFER_URL + reservation.getId() + "/accept )" + changeLine +
            "예약 변경을 원하지 않고 바로 전액 환불을 원하신다면 다음의 링크를 통해 눌러주시면 됩니다." + changeLine +
            "( " + TEMP_CHANGE_OFFER_URL + reservation.getId() + "/reject )" + changeTwoLine +
            "이 예약 변경 제안은 6시간 동안 유효하며 6시간 이후에는 자동으로 전액환불 됩니다." + changeTwoLine +
            "기대하셨을 여행에 실망을 안겨드려 다시 한 번 죄송합니다." + changeLine +
            "감사합니다.";

        String subject4Admin = "[한옥스테이 여여] 플랫폼 간 예약 중복으로 인한 예약 취소 발생";
        String content4Admin = "[한옥스테이 여여 관리자 알림 문자]" + changeTwoLine +
            "예약 정보 동기화 중 플랫폼 간 예약 중복이 발견되어 예약 변경 제안이 발생되었습니다." + changeLine +
            "예약 번호 : " + reservation.getId() + changeLine +
            "예약자 : " + reservation.getGuest().getName() + changeLine +
            "예약자 연락처 : " + reservation.getGuest().getPhoneNumber() + changeLine +
            "예약 일자 : " + startDate_string + " ~ " + endDate_string + changeLine +
            "예약 방 : " + room + changeLine;

        log.info("예약자에게 문자를 보냅니다. to : {}", to);
        sendMessage("LMS", subject, content, to);
        log.info("관리자에게 문자를 보냅니다. to : {}", ADMIN_LIST);
        sendMultipleMessage("LMS", subject4Admin, content4Admin, ADMIN_LIST);
    }

    private SendMessageResponseDto sendMessage(String type, String subject, String content, String to) {
        try {
            String uri = "/sms/v2/services/"+smsKey+"/messages";
            String url = NCLOUD_SMS_URL+uri;
            String timestamp = getTimestamp();
            String signature = getSignature("POST", uri, timestamp);

            String numberOnlyTo = getNumberOnly(to);
            if (isVaildPhoneNumber(numberOnlyTo) == false) {
                log.error("전화번호가 유효하지 않습니다. to : {}", to);
                return null;
            }
            return webClientService.sendMessage(type, url, subject, content, numberOnlyTo, timestamp, accessKey, signature);
        } catch (Exception e) {
            log.error("문자 발송 중 에러 발생", e);
            return null;
        }
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
        String newLine = System.lineSeparator();

        String message = new StringBuilder()
                .append(method).append(space)
                .append(uri).append(newLine)
                .append(timestamp).append(newLine)
                .append(accessKey).toString();

        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            log.error("Signature 생성 중 NoSuchAlgorithmException 에러 발생", e);
            return e.toString();
        } catch (InvalidKeyException e) {
            log.error("Signature 생성 중 InvalidKeyException 에러 발생", e);
            return e.toString();
        }
    }

    private String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String getAuthKey() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private String registerAuthKey(String phoneNumber, String authKey) {
        try {
            commonMethod.setCache(phoneNumber, authKey, 3);
            return null;
        } catch (RedisConnectionFailureException e) {
            String token = commonMethod.encodeToken(this.fallbackKey+phoneNumber, authKey);
            log.info("Redis 연결 실패로 토큰 생성 : {}", token);
            sendDevMsg("Redis 연결 실패로 토큰 생성 : " + phoneNumber);
            return token;
        }
    }

    private boolean checkAuthKey(String phoneNumber, String authKey, String authToken) {
        if (authToken != null) {
            return authToken.equals(commonMethod.encodeToken(this.fallbackKey+phoneNumber, authKey));
        }
        String redisAuthKey = commonMethod.getCache(phoneNumber);
        if (redisAuthKey == null) return false;
        if (redisAuthKey.equals(authKey)) {
            return commonMethod.delCache(phoneNumber);
        }
        return false;
    }

    private boolean isVaildPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 11 && phoneNumber.startsWith("010");
    }

    private String getNumberOnly(String string) {
        return string.replaceAll("[^0-9]","");
    }

    private boolean checkAdminName(String name) {
        // check name if exist in admin list
        return name.equals("관리자 생성 예약");
    }

    private boolean checkAdminPhone(String phoneNumber) {
        return ADMIN_LIST.contains(phoneNumber);
    }

}
