document.getElementById('submitButton').addEventListener('click', function () {
    const msisdn = document.getElementById('phone').value;
    const password = document.getElementById('password').value;

    const requestPayload = {
        msisdn: msisdn,
        password: password,
    };

    console.log("Request Payload:", requestPayload);

    fetch('http://localhost:8080/v1/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestPayload),
    })
        .then(response => response.json())
        .then(data => {
            if (data.access_token && data.refresh_token) {
                localStorage.setItem('access_token', data.access_token);
                localStorage.setItem('refresh_token', data.refresh_token);
                window.location.href = `/src/main/resources/static/UserInformationPage/UserInformationPage.html`;
            } else {
                console.log(data);
                alert('Login failed: ' + (data.message || 'Unknown error'));
            }
        })
        .catch((error) => {
            console.error('Error:', error);
            alert('Bir hata oluştu. Lütfen tekrar deneyiniz.');
        });
});