//const SERVER_URL = "http://localhost:8080"
//const SERVER_URL = "http://3.35.98.5:8080"
const SERVER_URL = "https://api.yeoyeo.co.kr"

function signup() {
    let userId = document.querySelector('#userId').value
    let userPassword = document.querySelector('#userPassword').value
    let userName = document.querySelector('#userName').value
    let userContact = document.querySelector('#userContact').value

    jQuery.ajax({
        url: SERVER_URL+"/admin/signup",
        method: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify({
            userId,
            userPassword,
            userName,
            userContact
        })
    }).done(function (data) {
        window.location.replace(SERVER_URL+"/login.html")
    }).fail(function(error) {
        console.log("실패")
        console.log(error)
    });
}