//const SERVER_URL = "http://localhost:8080"
const SERVER_URL = "https://api.yeoyeo.co.kr"

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

const subtract = (event) => {
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
    httpRequest.open('DELETE', SERVER_URL+'/admin/manage/info', true);
    /* Response Type을 Json으로 사전 정의 */
    httpRequest.responseType = "json";
    /* 요청 Header에 컨텐츠 타입은 Json으로 사전 정의 */
    httpRequest.setRequestHeader('Content-Type', 'application/json');
    /* 정의된 서버에 요청을 전송 */
    httpRequest.send(JSON.stringify(requestJson));
}

const notice = (event) => {
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
    httpRequest.open('POST', SERVER_URL+'/admin/manage/message/notice', true);
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
    for ( let i=0; i<result.length ; i++) {
        const tr1 = document.createElement('tr');
        const tr2 = document.createElement('tr');
        const td1 = document.createElement('td');
        td1.className = "fw-bold";
        const td2 = document.createElement('td');
        const td3 = document.createElement('td');
        const td4 = document.createElement('td');
        const td5 = document.createElement('td');
        const td6 = document.createElement('td');
        const td7 = document.createElement('td');
        const td8 = document.createElement('td');
        const tdButton = document.createElement('td');
        tdButton.setAttribute("rowspan", 2);

        tbody.append(tr1,tr2);
        if (result[i].guestType === 'DIRECT') {
            tr1.className = "table-primary";
            tr2.className = "table-primary";
            const input1 = document.createElement('input');
            const input2 = document.createElement('input');
            const input3 = document.createElement('input');
            const input4 = document.createElement('input');
            const buttonModify = document.createElement('button');
            const buttonDelete = document.createElement('button');
            const br = document.createElement('br');
            const buttonNotice = document.createElement('button');
            input1.id = "checkOut"+i;
            input1.value = result[i].checkOut;
            input2.id = "guestName"+i;
            input2.value = result[i].guestName;
            input3.id = "guestPhoneNumber"+i;
            input3.value = result[i].guestPhoneNumber;
            input4.id = "guestCount"+i;
            input4.value = result[i].guestCount;
            buttonModify.className = "btn btn-outline-primary";
            buttonModify.id = result[i].reservationId;
            buttonModify.value = i;
            buttonModify.innerText = "수정";
            buttonModify.onclick = function(event) { modify(event) };
            buttonDelete.className = "btn btn-outline-danger";
            buttonDelete.id = result[i].reservationId;
            buttonDelete.value = i;
            buttonDelete.innerText = "삭제";
            buttonDelete.onclick = function(event) { subtract(event) };
            buttonNotice.className = "btn btn-outline-success";
            buttonNotice.id = result[i].reservationId;
            buttonNotice.value = i;
            buttonNotice.innerText = "문자전송";
            buttonNotice.onclick = function(event) { notice(event) };

            tr1.append(td1, td2, td3, td4, tdButton);
            tr2.append(td5, td6, td7, td8);

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
            tdButton.append(buttonModify, buttonDelete, br, buttonNotice);

            const checkOut = new Date(result[i].checkOut);
            const reservationCheckOut = new Date(result[i].reservationCheckOut);

            if (checkOut < reservationCheckOut) {
                let isAddButton = true;
                for ( let j=i+1; j<result.length ; j++) {
                    if (new Date(result[j].checkIn) >= reservationCheckOut) {
                        break;
                    } else if (result[j].reservationId == result[i].reservationId) {
                        isAddButton = false;
                        break;
                    }
                }
                if (isAddButton) {
                    const tr1Add = document.createElement('tr');
                    const tr2Add = document.createElement('tr');
                    tr1Add.className = "table-warning";
                    tr2Add.className = "table-warning";
                    const td1Add = document.createElement('td');
                    const td2Add = document.createElement('td');
                    const td3Add = document.createElement('td');
                    const td4Add = document.createElement('td');
                    const td5Add = document.createElement('td');
                    const td6Add = document.createElement('td');
                    const td7Add = document.createElement('td');
                    const td8Add = document.createElement('td');
                    const tdButtonAdd = document.createElement('td');
                    tdButtonAdd.setAttribute("rowspan", 2);

                    tbody.append(tr1Add,tr2Add);

                    const input1Add = document.createElement('input');
                    const input2Add = document.createElement('input');
                    const input3Add = document.createElement('input');
                    const input4Add = document.createElement('input');
                    const buttonAdd = document.createElement('button');
                    input1Add.id = "checkOutAdd"+i;
                    checkOut.setDate(checkOut.getDate() + 1);
                    input1Add.value = dateFormat(checkOut);
                    input2Add.id = "guestNameAdd"+i;
                    input2Add.placeholder = "이름"
                    input3Add.id = "guestPhoneNumberAdd"+i;
                    input3Add.placeholder = "전화번호"
                    input4Add.id = "guestCountAdd"+i;
                    input4Add.placeholder = "인원"
                    buttonAdd.className = "btn btn-outline-warning";
                    buttonAdd.id = result[i].reservationId;
                    buttonAdd.value = i;
                    buttonAdd.innerText = "추가";
                    buttonAdd.onclick = function(event) { add(event) };

                    tr1Add.append(td1Add, td2Add, td3Add, td4Add, tdButtonAdd);
                    tr2Add.append(td5Add, td6Add, td7Add, td8Add);

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
                    tdButtonAdd.appendChild(buttonAdd);
                }
            }
        }
        else {
            tr1.append(td1, td2, td3, td4, tdButton);
            tr2.append(td5, td6, td7, td8);

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
