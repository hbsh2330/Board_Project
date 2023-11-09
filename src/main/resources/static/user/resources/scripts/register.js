const mainForm = document.getElementById('main');
const addressFind = document.getElementById('addressFind');
mainForm['prev'].onclick = function () {
    const minStep = 1;
    const maxStep = 3;
    let step = parseInt(mainForm.dataset.step) - 1;
    if (step < minStep) {
        step = minStep;
    }
    if (step > maxStep) {
        step = maxStep;
    }
    mainForm.dataset.step = step + '';
}

mainForm['infoEmailSend'].onclick = function () {
    if (mainForm['infoEmail'].value === '') {
        dialog.show({
            title: '이메일',
            content: '이메일을 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    mainForm['infoEmail'].focus();
                    dialog.hide();
                })
            ]
        });
        return;
    }
    if (!new RegExp(mainForm['infoEmail'].dataset.regex).test(mainForm['infoEmail'].value)) {
        dialog.show({
            title: '이메일',
            content: '올바른 이메일을 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    mainForm['infoEmail'].focus();
                    mainForm['infoEmail'].select();
                    dialog.hide();
                })
            ]
        });
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', mainForm['infoEmail'].value); // name=email을 찾아 mainForm의 값을 넣어 서버로 전달
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) { //만약 준비단계 완료되지 않을 경우 return한다.
            return;
        }
        loading.hide(); // 준비단계가 완료되었을 경우 loding을 숨기고
        if (xhr.status >= 200 && xhr.status < 300) { //만약 상태코드가 200보다 크거나 상태코드가 300보다 작을경우 //서버가 구동될 경우
            const responseObject = JSON.parse(xhr.responseText); //UserController에서 받은 responseObject text를 제이슨으로 파싱해서 resonseObject에 담는다.
            switch (responseObject['result']) { //키가 result인 것의 값이 failure이면
                case 'failure':
                    dialog.show({ //dialog.show함수를 실행한다.
                        title: '오류', // 제목: 오류
                        content: '알 수 없는 이유로 인증번호를 전송하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.', //내용
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    }); //버튼을 만들고 확인 하고 dialog를 숨긴다.
                    break; //빠져나간다.
                case 'failure_duplicate_email': //만약 키가 result인 것의 값이 failure_duplicate_email이면
                    dialog.show({ //dialog.show함수를 실행한다.
                        title: '오류', // 제목 오류
                        content: '해당 이메일은 이미 사용 중입니다.',
                        buttons: [
                            dialog.createButton('확인', function () {
                                mainForm['infoEmail'].focus();
                                mainForm['infoEmail'].select();
                                dialog.hide();
                            })
                        ]
                    });
                    break;
                case 'success':
                    mainForm['infoEmailSalt'].value = responseObject['salt'];
                    mainForm['infoEmail'].setAttribute('disabled', '');
                    mainForm['infoEmailSend'].setAttribute('disabled', '');
                    mainForm['infoEmailCode'].removeAttribute('disabled');
                    mainForm['infoEmailVerify'].removeAttribute('disabled');
                    dialog.show({
                        title: '성공',
                        content: '입력하신 이메일로 인증번호가 포함된 메일을 전송하였습니다.<br><br>해당 인증번호는 <b>5분간만 유효</b>하니 유의해 주세요.',
                        buttons: [
                            dialog.createButton('확인', function () {
                                dialog.hide();
                                mainForm['infoEmailCode'].focus();
                            })
                        ]
                    });
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }
        } else {
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
        }
    }
    xhr.open('POST', './registerEmail'); // 요청이 Post고 url주소가 registerEmail
    xhr.send(formData); //요청을 내 보낸다.
    loading.show();
}

mainForm['infoEmailVerify'].onclick = function () {
    if (mainForm['infoEmailCode'].value === '') {
        dialog.show({
            title: '이메일 인증번호',
            content: '이메일 인증번호를 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    dialog.hide();
                    mainForm['infoEmailCode'].focus();
                })
            ]
        });
        return;
    }
    if (!new RegExp(mainForm['infoEmailCode'].dataset.regex).test(mainForm['infoEmailCode'].value)) {
        dialog.show({
            title: '이메일 인증번호',
            content: '올바른 이메일 인증번호를 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    dialog.hide();
                    mainForm['infoEmailCode'].focus();
                    mainForm['infoEmailCode'].select();
                })
            ]
        });
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', mainForm['infoEmail'].value);
    formData.append('code', mainForm['infoEmailCode'].value);
    formData.append('salt', mainForm['infoEmailSalt'].value);
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status >= 200 && xhr.status < 300) {
            const responseObject = JSON.parse(xhr.responseText);
            switch (responseObject['result']) {
                case 'failure':
                    dialog.show({
                        title: '오류',
                        content: '알 수 없는 이유로 이메일 인증번호를 확인하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'failure_expired':
                    dialog.show({
                        title: '오류',
                        content: '이메일 인증번호 세션이 만료되었습니다.<br><br>아래 확인 버튼을 눌러 이메일 인증을 재진행해 주세요.',
                        buttons: [
                            dialog.createButton('확인', function () {
                                dialog.hide();
                                mainForm['infoEmailSalt'].value = '';
                                mainForm['infoEmail'].removeAttribute('disabled');
                                mainForm['infoEmail'].focus();
                                mainForm['infoEmail'].select();
                                mainForm['infoEmailSend'].removeAttribute('disabled');
                                mainForm['infoEmailCode'].value = '';
                                mainForm['infoEmailCode'].setAttribute('disabled', '');
                                mainForm['infoEmailVerify'].setAttribute('disabled', '');
                            })
                        ]
                    });
                    break;
                case 'failure_invalid_code':
                    dialog.show({
                        title: '오류',
                        content: '이메일 인증번호가 올바르지 않습니다.<br><br>입력하신 인증번호를 다시 확인해 주세요.',
                        buttons: [
                            dialog.createButton('확인', function () {
                                dialog.hide();
                                mainForm['infoEmailCode'].focus();
                                mainForm['infoEmailCode'].select();
                            })
                        ]
                    });
                    break;
                case 'success':
                    mainForm.querySelector('[rel="infoEmailComplete"]').classList.add('visible');
                    dialog.show({
                        title: '이메일 인증',
                        content: '이메일 및 인증번호를 확인하였습니다.',
                        buttons: [
                            dialog.createButton('확인', function () {
                                dialog.hide();
                                mainForm['infoEmailCode'].setAttribute('disabled', '');
                                mainForm['infoEmailVerify'].setAttribute('disabled', '');
                            })
                        ]
                    });
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }
        } else {
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
        }
    };
    xhr.open('PATCH', './registerEmail');
    xhr.send(formData);
}

mainForm['infoAddressFind'].onclick = function () {
    new daum.Postcode({
        width : '100%',
        height : '100%',
        oncomplete : function (data){
            mainForm['infoAddressPostal'].value = data['zonecode'];
            mainForm['infoAddressPrimary'].value = data['address'];
            mainForm['infoAddressSecondary'].focus();
            mainForm['infoAddressSecondary'].select();
            console.log(data)
            addressFind.classList.remove('visible')
        }
    }).embed(addressFind.querySelector(':scope > .modal'));
    addressFind.classList.add('visible')
};

mainForm.onsubmit = function (e) {
    e.preventDefault();
    switch (parseInt(mainForm.dataset.step)) {
        case 1:
            if (!mainForm['termPolicyAgree'].checked) {
                dialog.show({
                    title: '서비스 이용약관',
                    content: '서비스 이용약관을 읽고 동의해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                return false;
            }
            if (!mainForm['termPrivacyAgree'].checked) {
                dialog.show({
                    title: '개인정보 처리방침',
                    content: '개인정보 처리방침을 읽고 동의해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                return false;
            }
            if (!mainForm['termMarketingAgree'].checked) {
                dialog.show({
                    title: '마케팅 및 광고 활용 동의',
                    content: '마케팅 및 광고 활용에 동의하시면 다양한 혜택을 받아보실 수 있습니다.<br><br>다시 확인해 보시려면 <b>닫기</b>버튼을, 동의하지 않고 진행하시려면 <b>계속하기</b>버튼을 클릭해 주세요.',
                    buttons: [
                        dialog.createButton('닫기', dialog.hide),
                        dialog.createButton('계속하기', function () {
                            mainForm.dataset.step = '2';
                            dialog.hide();
                        })
                    ]
                });
            } else {
                mainForm.dataset.step = '2';
            }
            break;
        case 2:
            break;
        case 3:
            break;
    }
}

addressFind.querySelector('[rel=close]').onclick = function (){
    addressFind.classList.remove('visible')
}