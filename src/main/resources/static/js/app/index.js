function requestPay() {
    // 고객 입력정보
    const name = '박정웅'
    const phoneNumber = '010-2033-9091'
    const email = 'dvlprjw@gmail.com'
    const guestCount = 2
    const request = "특별한 건 없습니다."

    // 아임포트
    var IMP = window.IMP; // 생략 가능
    IMP.init("imp28607423"); // 예: imp00000000

    // IMP.request_pay(param, callback) 결제창 호출
    IMP.request_pay({ // param
        pg: "kcp",
        pay_method: "card",
        merchant_uid: "resv_no_0003", //고유 주문번호
        name: "여여 결제 테스트",
        amount: 277777, // 결제금액
        buyer_email: "toto9091@naver.com",
        buyer_name: "박정웅",
        buyer_tel: "010-1234-7777",
        buyer_addr: "서울특별시 강남구 신사동",
        buyer_postcode: "01181"
    }, function (rsp) { // callback
        if (rsp.success) {
            alert("결제에 성공하였습니다.");
            console.log(rsp);
            // 결제 성공 시: 결제 승인 또는 가상계좌 발급에 성공한 경우
            // jQuery로 HTTP 요청
            jQuery.ajax({
                url: "http://localhost:8080/reservation/payment", // 예: https://www.myservice.com/payments/complete
                method: "POST",
                headers: { "Content-Type": "application/json" },
                data: JSON.stringify({
                    imp_uid: rsp.imp_uid,
                    merchant_uid: rsp.merchant_uid,
                    name,
                    phoneNumber,
                    email,
                    guestCount,
                    request
                })
            }).done(function (data) {
                alert("서버 응답 성공!")
                // 가맹점 서버 결제 API 성공시 로직
                switch(data.status) {
                    case "vbankIssued":
                      // 가상계좌 발급 시 로직
                      break;
                    case "success":
                      // 결제 성공 시 로직
                      break;
                }
            })
      } else {
        alert("결제에 실패하였습니다. 에러 내용: " +  rsp.error_msg);
      }
  });
}