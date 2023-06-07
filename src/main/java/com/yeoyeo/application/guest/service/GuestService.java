package com.yeoyeo.application.guest.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.guest.dto.ImpCertRequestDto;
import com.yeoyeo.application.guest.dto.ImpCertResponseDto;
import com.yeoyeo.application.guest.etc.exception.GuestException;
import com.yeoyeo.application.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GuestService {

    String IMP_GET_CERT_URL = "https://api.iamport.kr/certifications/";

    private final WebClientService webClientService;
    private final PaymentService paymentService;

    public GeneralResponseDto checkCertification(ImpCertRequestDto requestDto) {
        try {
            String accessToken = paymentService.getToken();
            ImpCertResponseDto certificationInfo = getCertificationData(requestDto.getImp_uid(), accessToken);
            log.info(certificationInfo.toString());
            return GeneralResponseDto.builder().success(true).message("인증되었습니다.").build();
        } catch (GuestException guestException) {
            return GeneralResponseDto.builder().success(false).message(guestException.getMessage()).build();
        }
    }

    private ImpCertResponseDto getCertificationData(String imp_uid, String accessToken) throws GuestException {
        ImpCertResponseDto response = webClientService.WebClient("application/json").get()
                .uri(IMP_GET_CERT_URL+imp_uid)
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(ImpCertResponseDto.class)
                .block();
//        ImpCertResponseDto test = webClientService.getWithAuth("application/json", IMP_GET_CERT_URL+imp_uid, accessToken, ImpCertResponseDto.class);
        if (response == null) throw new GuestException("IAMPORT Return 데이터 문제");
        else if (response.getCode() != 0) throw new GuestException("유효하지 않은 인증입니다.");
        return response;
    }

}
