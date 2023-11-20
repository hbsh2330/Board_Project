if (document.head.querySelector(':scope > meta[name="_board-status"]').getAttribute('content') == 'false') { //존재하지 않는 게시판이면
    dialog.show({
        title: '경고',
        content: '존재하지 않는 게시판입니다.',
        buttons: [dialog.createButton('확인', function () {
            if (history.length > 1) {
                history.back(); // 뒤로가는 거
            } else {
                window.close(); // 화면을 닫는다.
            }
        })]
    })
} else if (document.head.querySelector(':scope > meta[name="_allowed-status"]').getAttribute('content') === 'false') {
    dialog.show({
        title: '경고',
        content: '해당 게시판에 게시글을 작성할 권한이 없습니다.',
        buttons: [dialog.createButton('확인', function () {
            if (history.length > 1) {
                history.back(); // 뒤로가는 거
            } else {
                window.close(); // 화면을 닫는다.
            }
        })]
    })
}

const writeForm = document.getElementById('writeForm');

if (writeForm) { //null도 아니고 undefinde도 아닌것 만약 존재하면
    ClassicEditor.create(writeForm['content'], {})
        .then(function (editor) {
            writeForm.editor = editor;
        })
        .catch(function (error) {
            console.log(error);
        });

    writeForm.onsubmit = function (e) {
        e.preventDefault();

        if (writeForm['title'].value === '') {
            dialog.show({
                title: '경고',
                content: '제목을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    writeForm['title'].focus();
                })]
            });
            return false;
        }
        if (!writeForm['title'].testRegex()) {
            dialog.show({
                title: '경고',
                content: '올바른 제목을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    writeForm['title'].focus();
                    writeForm['title'].select();
                })]
            });
            return false;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('boardCode', writeForm['code'].value); //boardCode로 해야지 ArticleEntity가 boradCode로 맴버변수로 받기 때문에 writeForm['code']는 html의 name=code
        formData.append('title', writeForm['title'].value);
        formData.append('content', writeForm.editor.getData());
        xhr.onreadystatechange = function () {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            loading.hide();
            if (xhr.status < 200 || xhr.status >= 300) {
                dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                return;
            }
            const responseObject = JSON.parse(xhr.responseText);
            switch (responseObject['result']) {
                case 'failure':
                    dialog.show({
                        title: '오류',
                        content: '알수없는 이유로 게시글 작성에 실패하였습니다.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'success':
                    location.href = `./read?index=${responseObject['index']}`;// 자기가 쓴 게시판 번호로 이동
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다..<br><br>잠시후 다시시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }

        }
        xhr.open('POST', './write');
        xhr.send(formData);
        loading.show();
    }
}
