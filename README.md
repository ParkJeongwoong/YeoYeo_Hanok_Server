[Todo]

- 휴대폰 인증 검증 (다날이랑 계약 후 사용 가능. 정액제 요금)
- 웹훅

[Webhook 경우의 수]
1. 예약 -> Webhook
   - Reservation이 있는지 확인
   - Payment가 있는지 확인


2. Webhook -> 예약




3. Webhook only


[결제 경우의 수]
예약이 우선 / WEBHOOK은 후순위
Q. WEBHOOK 도착 후 1분이 지나서 환불된 이후 예약이 되는 경우는?
A. validate 함수에 status check가 있어서 괜찮음
**통과 조건 필요** (예약된 건에 대해 환불이 안 되도록)

- 1예약 1webhook 2예약 2webhook => 1예약, 1webhook 검증완료(통과조건), 2예약 검증실패 환불(조건2), 2webhook 검증실패(조건1)
- 1예약 2예약 1webhook 2webhook => 1예약, 2예약 검증실패 환불(조건2), 1webhook 검증완료(통과조건), 2webhook 검증실패(조건1)
- 1예약 2예약 2webhook 1webhook => 1예약, 2예약 검증실패 환불(조건2), 2webhook 검증실패(조건1), 1webhook 검증완료(통과조건)
- 1예약 2webhook 2예약 1webhook => 1예약, 2webhook 검증실패 환불(조건1), 2예약 검증실패 환불(조건2), 1webhook 검증완료(통과조건)
- 1webhook 1예약 2webhook 2예약 => 1분 Lock, 1예약, 2webhook 검증실패 환불(조건1), 2예약 검증실패 환불(조건2)
- 1webhook 2webhook 1예약 2예약 => 1분 Lock, 1예약, 2예약 검증실패 환불(조건2)
- 1webhook 2webhook 2예약 1예약 => 1분 Lock, 2예약, 1예약 검증실패 환불(조건2)
- 1webhook 2예약 2webhook 1예약 => 1분 Lock, 2예약, 2webhook 검증완료(통과조건), 1예약 검증실패 환불(조건2)

[각 작업]
예약 : DATEROOM BOOKED, RESV. COUNT+1 / RESERVATION CREATE / PAYMENT CREATE / GUEST CREATE 
webhook : DATEROOM (IF NOT BOOKED) BOOKED FOR 1 MINUTE => (WHEN REFUND) RESV. COUNT+1