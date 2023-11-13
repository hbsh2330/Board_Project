const loginForm = document.getElementById('loginForm');

if (typeof localStorage.getItem('loginEmail') === 'string'){ // 값이 있을때
    loginForm['email'].value = localStorage.getItem('loginEmail');
    loginForm['password'].focus();
    loginForm['remember'].checked = true;
}
loginForm.onsubmit = function (e) {
    e.preventDefault();

    if (loginForm['email'].value === '') {
        dialog.show({
            title: '로그인',
            content: '이메일을 입력해 주세요',
            buttons: [
                dialog.createButton('확인', function () {
                    dialog.hide();
                    loginForm['email'].focus();
                })
            ]
        });
        return false;
    }
    // if (loginForm['email'].value === ''){
    //     dialog.show({
    //         title : '로그인',
    //         content : '이메일을 입력해 주세요',
    //         buttons : [
    //             dialog.createButton('확인', function (){
    //                 dialog.hide();
    //                 loginForm['email'].focus();
    //             })
    //         ]
    //     });
    //     return false;
    // }
    // if (loginForm['email'].value() === ''){
    //     dialog.show({
    //         title : '로그인',
    //         content : '이메일을 입력해 주세요',
    //         buttons : [
    //             dialog.createButton('확인', function (){
    //                 dialog.hide();
    //                 loginForm['email'].focus();
    //             })
    //         ]
    //     });
    //     return false;
    // }
    // if (loginForm['email'].value === ''){
    //     dialog.show({
    //         title : '로그인',
    //         content : '이메일을 입력해 주세요',
    //         buttons : [
    //             dialog.createButton('확인', function (){
    //                 dialog.hide();
    //                 loginForm['email'].focus();
    //             })
    //         ]
    //     });
    //     return false;
    // }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', loginForm['email'].value)
    formData.append('password', loginForm['password'].value)
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return; //만약 xhr의 준비가 완료되지 않았다면 retun
        }
        loading.hide();
        if (xhr.status >= 200 && xhr.status < 300) { //만약 상태코드가 200보다 크거나, 상태코드가 300보다 작으면
            const responseObject = JSON.parse(xhr.responseText);
            switch (responseObject['result']) {
                case 'failure':
                    dialog.show({
                        title: '경고',
                        content: '해당 계정에 접속이 실패되었습니다.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'failure_suspended':
                    dialog.show({
                        title: '경고',
                        content: '해당 이메일이 다릅니다. 다릅니다..',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'success':
                    if (loginForm['remember'].checked) {
                        localStorage.setItem('loginEmail', loginForm['email'].value); // 다음부터 이메일을 기억합니다. 체크박스가 체크가 되었을 경우 localStorage : key와 value를 받는 session과 비슷하지만 껏다켜도 기억이 유지가 되는 것에 key로 loginEmail 값으로 email의값을 넣는다.
                    }
                    location.href = '../'
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }
        }
    }
    xhr.open('POST', './login');
    xhr.send(formData);
    loading.show();
}