const SERVER_URL = "http://localhost:8080"
//const SERVER_URL = "https://api.yeoyeo.co.kr"

const today = new Date().toLocaleDateString();
//document.write("<h1>오늘 날짜 : "+today+"</h1>")
todayDiv = document.getElementById("today");
todayDiv.innerHTML = "<h1>오늘 날짜 : "+today+"</h1>";


// 요청 전송
window.onload = function() {
	var httpRequest;
    /* 통신에 사용 될 XMLHttpRequest 객체 정의 */
    httpRequest = new XMLHttpRequest();
    /* httpRequest의 readyState가 변화했을때 함수 실행 */
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
              if (httpRequest.status === 200) {
                var result = httpRequest.response;
                console.log(result)
                makeTable(result);
              } else {
                alert('Request Error!');
              }
        }
    };
    /* Get 방식으로 name 파라미터와 함께 요청 */
    httpRequest.open('GET', SERVER_URL+'/admin/manage/info');
    /* Response Type을 Json으로 사전 정의 */
    httpRequest.responseType = "json";
    /* 정의된 서버에 요청을 전송 */
    httpRequest.send();
}

const modify = (event) => {
    const checkIn = document.querySelector("#checkIn"+event.target.value).textContent;
    const roomName = document.querySelector("#roomName"+event.target.value).textContent;
    let roomId = 0;
    if (roomName === "여유") roomId = 1;
    else if (roomName === "여행") roomId = 2;

    const checkOut = document.querySelector("#checkOut"+event.target.value);
    const guestName = document.querySelector("#guestName"+event.target.value);
    const guestPhoneNumber = document.querySelector("#guestPhoneNumber"+event.target.value);
    const guestCount = document.querySelector("#guestCount"+event.target.value);

    const requestJson = new Object();
    requestJson.checkIn = checkIn;
    requestJson.roomId = roomId;
    requestJson.checkOut = checkOut.value;
    requestJson.guestName = guestName.value;
    requestJson.guestPhoneNumber = guestPhoneNumber.value;
    requestJson.guestCount = guestCount.value;
    requestJson.reservationId = event.target.id;

	var httpRequest;
    /* 통신에 사용 될 XMLHttpRequest 객체 정의 */
    httpRequest = new XMLHttpRequest();
    /* httpRequest의 readyState가 변화했을때 함수 실행 */
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
              if (httpRequest.status === 200) {
                var result = httpRequest.response;
                location.reload();
              } else {
                alert('Request Error!');
              }
        }
    };
    /* Get 방식으로 name 파라미터와 함께 요청 */
    httpRequest.open('PUT', SERVER_URL+'/admin/manage/info', true);
    /* Response Type을 Json으로 사전 정의 */
    httpRequest.responseType = "json";
    /* 요청 Header에 컨텐츠 타입은 Json으로 사전 정의 */
    httpRequest.setRequestHeader('Content-Type', 'application/json');
    /* 정의된 서버에 요청을 전송 */
    httpRequest.send(JSON.stringify(requestJson));
}

const add = (event) => {
    const checkIn = document.querySelector("#checkInAdd"+event.target.value).textContent;
    const roomName = document.querySelector("#roomNameAdd"+event.target.value).textContent;
    let roomId = 0;
    if (roomName === "여유") roomId = 1;
    else if (roomName === "여행") roomId = 2;

    const checkOut = document.querySelector("#checkOutAdd"+event.target.value);
    const guestName = document.querySelector("#guestNameAdd"+event.target.value);
    const guestPhoneNumber = document.querySelector("#guestPhoneNumberAdd"+event.target.value);
    const guestCount = document.querySelector("#guestCountAdd"+event.target.value);

    const requestJson = new Object();
    requestJson.checkIn = checkIn;
    requestJson.roomId = roomId;
    requestJson.checkOut = checkOut.value;
    requestJson.guestName = guestName.value;
    requestJson.guestPhoneNumber = guestPhoneNumber.value;
    requestJson.guestCount = guestCount.value;
    requestJson.reservationId = event.target.id;

	var httpRequest;
    /* 통신에 사용 될 XMLHttpRequest 객체 정의 */
    httpRequest = new XMLHttpRequest();
    /* httpRequest의 readyState가 변화했을때 함수 실행 */
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
              if (httpRequest.status === 200) {
                var result = httpRequest.response;
                location.reload();
              } else {
                alert('Request Error!');
              }
        }
    };
    /* Get 방식으로 name 파라미터와 함께 요청 */
    httpRequest.open('POST', SERVER_URL+'/admin/manage/info', true);
    /* Response Type을 Json으로 사전 정의 */
    httpRequest.responseType = "json";
    /* 요청 Header에 컨텐츠 타입은 Json으로 사전 정의 */
    httpRequest.setRequestHeader('Content-Type', 'application/json');
    /* 정의된 서버에 요청을 전송 */
    httpRequest.send(JSON.stringify(requestJson));
}

// 테이블 생성
const makeTable = (result) => {
    const tbody = document.querySelector('tbody');
    //테이블 생성 태그를 연다.

    // for문 시작
    for ( var i=0; i<result.length ; i++) {
        const tr = document.createElement('tr');
        const td1 = document.createElement('td');
        td1.className = "fw-bold";
        const td2 = document.createElement('td');
        const td3 = document.createElement('td');
        const td4 = document.createElement('td');
        const td5 = document.createElement('td');
        const td6 = document.createElement('td');
        const td7 = document.createElement('td');
        const td8 = document.createElement('td');
        const td9 = document.createElement('td');

        tbody.appendChild(tr);
        if (result[i].guestType === 'DIRECT') {
            tr.className = "table-primary";
            const input1 = document.createElement('input');
            const input2 = document.createElement('input');
            const input3 = document.createElement('input');
            const input4 = document.createElement('input');
            const button = document.createElement('button');
            input1.id = "checkOut"+i;
            input1.value = result[i].checkOut;
            input2.id = "guestName"+i;
            input2.value = result[i].guestName;
            input3.id = "guestPhoneNumber"+i;
            input3.value = result[i].guestPhoneNumber;
            input4.id = "guestCount"+i;
            input4.value = result[i].guestCount;
            button.className = "btn btn-outline-primary";
            button.id = result[i].reservationId;
            button.value = i;
            button.innerText = "수정";
            button.onclick = function(event) { modify(event) };

            tr.append(td1, td2, td3, td4, td5, td6, td7, td8, td9);

            td1.innerText = result[i].checkIn;
            td1.id = "checkIn"+i;
            td2.appendChild(input1);
            td3.innerText = result[i].roomName;
            td3.id = "roomName"+i;
            td4.innerText = result[i].guestType;
            td5.appendChild(input2);
            td6.appendChild(input3);
            td7.appendChild(input4);
            td8.innerText = result[i].request;
            td9.appendChild(button);

            const checkOut = new Date(result[i].checkOut);
            const reservationCheckOut = new Date(result[i].reservationCheckOut);

            if (checkOut < reservationCheckOut && i+1 < result.length && result[i].reservationId != result[i+1].reservationId) {
                const trAdd = document.createElement('tr');
                trAdd.className = "table-warning";
                const td1Add = document.createElement('td');
                const td2Add = document.createElement('td');
                const td3Add = document.createElement('td');
                const td4Add = document.createElement('td');
                const td5Add = document.createElement('td');
                const td6Add = document.createElement('td');
                const td7Add = document.createElement('td');
                const td8Add = document.createElement('td');
                const td9Add = document.createElement('td');

                tbody.appendChild(trAdd);

                const input1Add = document.createElement('input');
                const input2Add = document.createElement('input');
                const input3Add = document.createElement('input');
                const input4Add = document.createElement('input');
                const buttonAdd = document.createElement('button');
                input1Add.id = "checkOutAdd"+i;
                checkOut.setDate(checkOut.getDate() + 1);
                input1Add.value = dateFormat(checkOut);
                input2Add.id = "guestNameAdd"+i;
                input2Add.value = result[i].guestName;
                input3Add.id = "guestPhoneNumberAdd"+i;
                input3Add.value = result[i].guestPhoneNumber;
                input4Add.id = "guestCountAdd"+i;
                input4Add.value = result[i].guestCount;
                buttonAdd.className = "btn btn-outline-warning";
                buttonAdd.id = result[i].reservationId;
                buttonAdd.value = i;
                buttonAdd.innerText = "추가";
                buttonAdd.onclick = function(event) { add(event) };

                trAdd.append(td1Add, td2Add, td3Add, td4Add, td5Add, td6Add, td7Add, td8Add, td9Add);

                td1Add.innerText = result[i].checkOut;
                td1Add.id = "checkInAdd"+i;
                td2Add.appendChild(input1Add);
                td3Add.innerText = result[i].roomName;
                td3Add.id = "roomNameAdd"+i;
                td4Add.innerText = result[i].guestType;
                td5Add.appendChild(input2Add);
                td6Add.appendChild(input3Add);
                td7Add.appendChild(input4Add);
                td8Add.innerText = result[i].request;
                td9Add.appendChild(buttonAdd);
            }
        }
        else {
            tr.append(td1, td2, td3, td4, td5, td6, td7, td8, td9);

            td1.innerText = result[i].checkIn;
            td2.innerText = result[i].checkOut;
            td3.innerText = result[i].roomName;
            td4.innerText = result[i].guestType;
            td5.innerText = result[i].guestName;
            td6.innerText = result[i].guestPhoneNumber;
            td7.innerText = result[i].guestCount;
            td8.innerText = result[i].request;
        }
    }
}

const dateFormat = (date) => {
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    return `${year}-${month}-${day}`;
}
