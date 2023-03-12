//const SERVER_URL = "http://localhost:8080"
const SERVER_URL = "http://3.35.98.5:8080"

function login() {
    let userId = document.querySelector('#userId').value
    let userPassword = document.querySelector('#userPassword').value

    jQuery.ajax({
        url: SERVER_URL+"/admin/login",
        method: "GET",
        headers: { "Content-Type": "application/json" },
        data: {
            userId,
            userPassword
        }
    }).done(function (data) {
        window.location.replace(SERVER_URL+"/index.html")
    }).fail(function(error) {
        console.log("실패")
        console.log(error)
    });
}