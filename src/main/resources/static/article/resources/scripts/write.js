if (document.head.querySelector(':scope > meta[name="_board-status"]').getAttribute('content') == 'false'){ //존재하지 않는 게시판이면
    dialog.show({
        title : '경고',
        content : '존재하지 않는 게시판입니다.',
        buttons: [dialog.createButton('확인', function (){
            if (history.length > 1){
                history.back(); // 뒤로가는 거
            } else {
                window.close(); // 화면을 닫는다.
            }
        })]
    })
} else if (document.head.querySelector(':scope > meta[name="_allowed-status"]').getAttribute('content') === 'false'){
    dialog.show({
        title : '경고',
        content : '해당 게시판에 게시글을 작성할 권한이 없습니다.',
        buttons: [dialog.createButton('확인', function (){
            if (history.length > 1){
                history.back(); // 뒤로가는 거
            } else {
                window.close(); // 화면을 닫는다.
            }
        })]
    })
}

const writeForm = document.getElementById('writeForm');

if (writeForm){ //null도 아니고 undefinde도 아닌것 만약 존재하면
    ClassicEditor.create(writeForm['content'], {})
        .then(function (editor){
            writeForm.editor = editor;
        })
        .catch(function (error){
            console.log(error);
        });
}