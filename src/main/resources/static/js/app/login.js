const SERVER_URL = "https://api.yeoyeo.co.kr"

function login() {
    let userId = document.querySelector('#userId').value
    let userPassword = document.querySelector('#userPassword').value

    jQuery.ajax({
        url: SERVER_URL+"/admin/login",
        method: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify({
            userId,
            userPassword
        })
    }).done(function (data) {
//        window.location.replace(SERVER_URL+"/swagger-ui/index.html#")
        window.location.replace(SERVER_URL+"/adminManage.html")
    }).fail(function(error) {
        console.log("실패")
        console.log(error)
    });
}