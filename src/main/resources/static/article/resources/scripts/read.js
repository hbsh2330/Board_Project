const commentTable = document.getElementById('commentTable');

const comment = {};

comment.alterlike = function (commentIndex, status) {
    // 전달받은 commentIndex 댓글을 좋아요 상태를 수정할 수 있는 함수
    // status
    // - true 좋아요
    // - false 싫어요
    // -null/undefined : 중립
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('commentIndex', commentIndex);
    if (typeof status === 'boolean') {
        formData.append('status', status);
    }
    xhr.onreadystatechange = function(){
        if(xhr.readyState !== XMLHttpRequest.DONE){
            return;
        }
        if(xhr.status < 200 || xhr.status >= 300){
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
            return;
        }
        const responseObject = JSON.parse(xhr.responseText);
        switch (responseObject['reset']){
            case 'failure' :
                    dialog.show({
                        title: '경고',
                        content: '댓글을 입력해 주세요.',
                        buttons: [dialog.createButton('확인', function () {
                            dialog.hide();
                            commentForm['content'].focus();
                        })]
                    });
                    break
            case 'success' :
  // TODO 코드 보고 수정
        }

    }
    xhr.open('PUT', './comment');
    xhr.send(formData);
};
//대댓글 작성 코드
comment.append = function (allComments, targetComment, step) { // allComments 모든 댓글 targetComment 찍고자 하는 댓글
    step ??=0;
    const commentEl = new DOMParser().parseFromString(`
                    <div class="comment ${targetComment['isMine'] === true ? 'mine' : ''} ${typeof targetComment['commentIndex'] === 'number' ? 'sub' : ''}" rel="comment">
                        <div class="head">
                            <span class="nickname" rel="nickname">${targetComment['userNickname']}</span>
                            <span class="written-at" rel="writtenAt">${targetComment['at']}${targetComment['isModified'] === true ? '(수정됨)' : ''}</span>
                            <span class="spring"></span>
                            ${targetComment['isMine'] === true ? '<span class="action" rel="modify">수정</span>' : ''}
                            ${targetComment['isMine'] === true ? '<span class="action" rel="delete">삭제</span>' : ''}
                        </div>
                        <div class="body">${targetComment['content']}</div>
                        <div class="foot">
                            <span class="vote" rel="vote" data-vote="up">
                                <img alt="👍" class="icon" src="./resources/images/comment.vote.up.png">
                                <span class="value">??</span>
                            </span>
                            <span class="vote" rel="vote" data-vote="down">
                                <img alt="👎" class="icon" src="./resources/images/comment.vote.down.png">
                                <span class="value">??</span>
                            </span>
                            <span class="spring"></span>
                            <span class="action" rel="reply">답글 달기</span>
                            <span class="action" rel="replyCancel">답글 달기 취소</span>
                        </div>
                        <form class="reply-form" rel="replyForm">
                            <label class="label">
                                <textarea class="common-field" maxlength="1000" name="content" placeholder="답글을 입력해 주세요." data-regex="${commentForm['content'].getAttribute('data-regex')}"></textarea>
                            </label>
                            <input class="common-button" type="submit" value="답글달기">
                        </form>
                    </div>`, 'text/html').querySelector('[rel="comment"]');
    const replyForm = commentEl.querySelector('[rel="replyForm"]');
    const upVote = commentEl.querySelector('[rel="vote"][data-vote="up"]');
    const downVote = commentEl.querySelector('[rel="vote"][data-vote="down"]');
    replyForm.onsubmit = function (e) {
        e.preventDefault();
        if (replyForm['content'].value === '') {
            dialog.show({
                title: '경고',
                content: '댓글을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    replyForm['content'].focus();
                })]
            });
            return false;
        }
        if (!replyForm['content'].testRegex()) {
            dialog.show({
                title: '경고',
                content: '올바른 댓글을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    replyForm['content'].focus();
                    replyForm['content'].select();
                })]
            });
            return false;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('articleIndex', targetComment['articleIndex'] + '');
        formData.append('commentIndex', targetComment['index'] + '');
        formData.append('content', replyForm['content'].value);
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
                        content: '알 수 없는 이유로 댓글을 작성하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'success':
                    comment.load();
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }
        }
        xhr.open('POST', './comment');
        xhr.send(formData);
        loading.show();
    }
    commentEl.style.marginLeft = `${3 * step}rem`
    commentEl.querySelector('[rel="reply"]').onclick = function () {
        commentEl.classList.add('replying');
        replyForm['content'].focus();
    }
    commentEl.querySelector('[rel="replyCancel"]').onclick = function () {
        commentEl.classList.remove('replying')
    }
    upVote.onclick = function (){
        if (upVote.classList.contains('selected')){
            comment.alterlike(targetComment['index'], null);
        } else {
            comment.alterlike(targetComment['index'], true);
        }
    }
    downVote.onclick = function (){
        if (downVote.classList.contains('selected')){
            comment.alterlike(targetComment['index'], null);
        } else {
            comment.alterlike(targetComment['index'], false);
        }
    }
    const tbody = commentTable.querySelector(':scope > tbody');
    const tr = document.createElement('tr');
    const td = document.createElement('td');
    td.append(commentEl);
    tr.append(td)
    tbody.append(tr);

    const subComments = allComments.filter(x => x['commentIndex'] === targetComment['index']); //
    if (subComments.length > 0) {
        for (const subComment of subComments) {
            comment.append(allComments, subComment, Math.min(step + 1, 4));
        }
    }
};

comment.load = function () {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        commentTable.classList.remove('loading');
        if (xhr.status < 200 || xhr.status >= 300) {
            commentTable.classList.add('error');
            return;
        }
        const comments = JSON.parse(xhr.responseText);
        for (const commentObject of comments.filter(x => typeof x['commentIndex'] !== 'number')) {
            comment.append(comments, commentObject);
        }
        commentForm.querySelector('[rel="count"]').innerText = comments.length;
    }
    xhr.open('GET', `./comment?articleIndex=${commentForm['articleIndex'].value}`);
    xhr.send();
    commentForm.querySelector('[rel="count"]').innerText = '0';
    commentTable.querySelector(':scope > tbody').innerHTML = '';
    commentTable.classList.remove('error');
    commentTable.classList.add('loading');
}

const commentForm = document.getElementById('commentForm');

commentForm.querySelector('[rel="refresh"]').onclick = comment.load;

if (commentForm) {
    commentForm.onsubmit = function (e) {
        e.preventDefault();

        if (commentForm['content'].value === '') {
            dialog.show({
                title: '경고',
                content: '댓글을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    commentForm['content'].focus();
                })]
            });
            return false;
        }
        if (!commentForm['content'].testRegex()) {
            dialog.show({
                title: '경고',
                content: '올바른 댓글을 입력해 주세요.',
                buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    commentForm['content'].focus();
                    commentForm['content'].select();
                })]
            });
            return false;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('articleIndex', commentForm['articleIndex'].value);
        formData.append('content', commentForm['content'].value);
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
                        content: '알 수 없는 이유로 댓글을 작성하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
                    break;
                case 'success':
                    commentForm['content'].value = '';
                    commentForm['content'].focus();
                    comment.load();
                    break;
                default:
                    dialog.show({
                        title: '오류',
                        content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                        buttons: [dialog.createButton('확인', dialog.hide)]
                    });
            }
        }
        xhr.open('POST', './comment');
        xhr.send(formData);
        loading.show();
    }
}