package com.yeoyeo.application.guest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@Getter
public class ImpCertResponseDto {
    private long code;
    private String message;
    private HashMap<String, String> response;
    //    <response item>
//    private String name;
//    private String gender;
//    private String birth;
//    private String unique_key; // CI 값과 동일. 온라인 주민번호와 같은 개인고유식별키
//    private String unique_in_site; // DI 값과 동일. 상점아이디(사이트)별로 할당되는 식별키

    /*
    휴대폰 번호(phone) 및 통신사(carrier) 또는 외국인(foreigner) 여부는 개인정보 제공동의 약관을 사이트에 게재한 후 cs@portone.io로 신청하여 취득 가능
    (포트원 계약 후 다날PG사 요청 후 성인 되면 이용 가능)

    <요청 메일 양식>
        상호명 :
        사업자번호 :
        본인인증용 다날 상점ID(CPID) :
        업종 :
        필요사유 :
        개인정보취급방침 url : 앱서비스로 URL형태로 전달이 어려우신 경우 '개인정보취급방침' 경로를 캡쳐하여 전달주시기 바랍니다.

    <참고 - 포트원 이용 가맹점의 개인정보처리방침 적용 예시>
        (주)마플 : https://marpple.shop/kr/@/privacy
        (주)브레이브모바일 / 숨고 : https://soomgo.com/terms/privacy
        (주)마켓잇 : https://static.marketit.asia/static/privacy-terms.pdf
    */
}
