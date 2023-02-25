//const SERVER_URL = "http://localhost:8080"
const SERVER_URL = "http://3.35.98.5:8080"

// 고객 입력정보
const name = '박정웅'
const phoneNumber = '010-2033-9091'
const email = 'dvlprjw@gmail.com'
const guestCount = 2

function requestPay() {
    let amount = $('#amount').val()
    let merchant_uid = $('#merchant_uid').val()
    let request = $('#request').val()

    // 아임포트
    var IMP = window.IMP; // 생략 가능
    IMP.init("imp28607423"); // 예: imp00000000

    // IMP.request_pay(param, callback) 결제창 호출
    IMP.request_pay({ // param
        pg: "kcp",
//        pg: "kakaopay",
        pay_method: "card",
        merchant_uid: merchant_uid, //고유 주문번호 (날짜+방)
        name: "여여 결제 테스트",
        amount: amount, // 결제금액
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
                url: SERVER_URL+"/payment/pay", // 예: https://www.myservice.com/payments/complete
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
            }).fail(function(error) { // 환불 실패시 로직
                      alert("서버 결제 중 실패");
            })
      } else {
        alert("결제에 실패하였습니다. 에러 내용: " +  rsp.error_msg);
      }
  });
}

function cancelPay() {
    let reservationId = $('#reservationId').val()
    let phoneNumber = $('#phoneNumber').val()

    jQuery.ajax({
        "url": SERVER_URL+"/payment/refund",
        "type": "DELETE",
        "contentType": "application/json",
        "data": JSON.stringify({
            "reservationId": reservationId,
            "phoneNumber": phoneNumber,
            "reason": "테스트 결제 환불" // 환불사유
        }),
        "dataType": "json"
    }).done(function(result) {
        alert("환불 성공");
    }).fail(function(error) { // 환불 실패시 로직
        alert("환불 실패");
    });
}

function certificate() {

    // 아임포트
    var IMP = window.IMP; // 생략 가능
    IMP.init("imp28607423"); // 예: imp00000000

    // IMP.certification(param, callback) 호출
    IMP.certification({
    }, function (rsp) { // callback
        if (rsp.success) {
            jQuery.ajax({
                url: SERVER_URL+"/guest/certificate",
                method: "POST",
                headers: { "Content-Type": "application/json" },
                data: { imp_uid: rsp.imp_uid }
            });
        } else {
            alert("인증에 실패하였습니다. 에러 내용: " +  rsp.error_msg);
        }
    });

}