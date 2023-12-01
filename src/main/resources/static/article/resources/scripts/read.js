const articleTable = document.getElementById('articleTable');
{ //지역변수로 사용 변수를 스코프안에서만 사용하도록 함
    const deleteEl = articleTable.querySelector('[rel="delete"]');

    if (deleteEl) {
        const deleteFunc = function () {
            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('index', articleTable.dataset.index);
            xhr.onreadystatechange = function () {
                if (xhr.readyState !== XMLHttpRequest.DONE) {
                    return;
                }
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
                            content: '알 수 없는 이유로 게시글을 삭제하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                            buttons: [dialog.createButton('확인', dialog.hide)]
                        });
                        break;
                    case 'success':
                        location.href = `../board/list?code=${articleTable.dataset.boardCode}`;
                        break;
                    default:
                        dialog.show({
                            title: '오류',
                            content: '서버가 예상치 못한 응답을 반환하였습니다.',
                            buttons: [dialog.createButton('확인', dialog.hide)]
                        });
                }
            }
            xhr.open('DELETE', './read');
            xhr.send(formData);
        }
        deleteEl.onclick = function () {
            dialog.show({
                title: '댓글 삭제',
                content: '정말로 댓글을 삭제하겠습니까?',
                buttons: [dialog.createButton('취소', dialog.hide), dialog.createButton('삭제', function () {
                    deleteFunc();
                    dialog.hide();
                })]
            })
        }
    }
}

const commentTable = document.getElementById('commentTable');

const comment = {};

comment.alterLike = function (commentIndex, status) {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('commentIndex', commentIndex);
    if (typeof status === 'boolean') {
        formData.append('status', status);
    }
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
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
                    content: '알 수 없는 이유로 요청을 처리하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                break;
            case 'success':
                const commentEl = commentTable.querySelector(`[rel="comment"][data-index="${commentIndex}"]`);
                const upVoteEl = commentEl.querySelector('[data-vote="up"]');
                const downVoteEl = commentEl.querySelector('[data-vote="down"]');
                upVoteEl.querySelector('.value').innerText = responseObject['likeCount'];
                downVoteEl.querySelector('.value').innerText = responseObject['dislikeCount'];
                switch (responseObject['likeStatus']) {
                    case 0:
                        upVoteEl.classList.remove('selected');
                        downVoteEl.classList.remove('selected');
                        break;
                    case 1:
                        upVoteEl.classList.add('selected');
                        downVoteEl.classList.remove('selected');
                        break;
                    case -1:
                        upVoteEl.classList.remove('selected');
                        downVoteEl.classList.add('selected');
                        break;
                }
                break;
            default:
                dialog.show({
                    title: '오류',
                    content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
        }
    }
    xhr.open('PUT', './comment');
    xhr.send(formData);
}

comment.append = function (allComments, targetComment, step) {
    step ??= 0;
    const commentEl = new DOMParser().parseFromString(`
        <div class="comment
                    ${targetComment['isMine'] === true ? 'mine' : ''}
                    ${typeof targetComment['commentIndex'] === 'number' ? 'sub' : ''}
                    ${typeof targetComment['content'] === 'string' ? '' : 'deleted'}"
                data-index="${targetComment['index']}"
                rel="comment">
            <div class="head">
                <span class="nickname" rel="nickname">${targetComment['userNickname']}</span>
                <span class="written-at" rel="writtenAt">
                    ${targetComment['at']}
                    ${targetComment['isModified'] === true ? '(수정됨)' : ''}
                </span>
                <span class="spring"></span>
                ${typeof targetComment['content'] === 'string' && targetComment['isMine'] === true ? '<span class="action" rel="modify">수정</span>' : ''}
                ${typeof targetComment['content'] === 'string' && targetComment['isMine'] === true ? '<span class="action" rel="delete">삭제</span>' : ''}
                ${typeof targetComment['content'] === 'string' && targetComment['isMine'] === true ? '<span class="action" rel="modifyCancel">수정 취소</span>' : ''}
            </div>
            <div class="body">
            <span class="content" rel="content">${typeof targetComment['content'] === 'string' ? targetComment['content'] : '삭제된 댓글입니다.'}</span>
            ${typeof targetComment['content'] === 'string' ? `
                <form class="modify-form" rel="modifyForm">
                 <textarea class="common-field" maxlength="1000" name="content" placeholder="답글을 입력해 주세요."></textarea>
                    <input class="common-button" type="submit" value="댓글 수정">
                </form>` : ''}
            </div>
            ${typeof targetComment['content'] === 'string' ? `
            <div class="foot">
                <span class="vote ${targetComment['likeStatus'] === 1 ? 'selected' : ''}" rel="vote" data-vote="up">
                    <img alt="👍" class="icon" src="./resources/images/comment.vote.up.png">
                    <span class="value">${targetComment['likeCount']}</span>
                </span>
                <span class="vote ${targetComment['likeStatus'] === -1 ? 'selected' : ''}" rel="vote" data-vote="down">
                    <img alt="👎" class="icon" src="./resources/images/comment.vote.down.png">
                    <span class="value">${targetComment['dislikeCount']}</span>
                </span>
                <span class="spring"></span>
                <span class="action" rel="reply">답글 달기</span>
                <span class="action" rel="replyCancel">답글 달기 취소</span>
            </div>
            <form class="reply-form" rel="replyForm">
                <label class="label">
                    <textarea class="common-field" maxlength="1000" name="content" placeholder="답글을 입력해 주세요." data-regex="${commentForm['content'].getAttribute('data-regex')}"></textarea> 
                </label>
                <!-- html for문 돌리기 위해서 사용-->
                <input class="common-button" type="submit" value="답글 달기">
            </form>` : ''}
        </div>`, 'text/html').querySelector('[rel="comment"]');
    commentEl.style.marginLeft = `${3 * step}rem`;

    const replyForm = commentEl.querySelector('[rel="replyForm"]');
    if (replyForm) {
        commentEl.querySelector('[rel="reply"]').onclick = function () {
            commentEl.classList.add('replying');
            replyForm['content'].focus();
        }
        commentEl.querySelector('[rel="replyCancel"]').onclick = function () {
            commentEl.classList.remove('replying');
        }
        replyForm.onsubmit = function (e) {
            e.preventDefault();
            if (replyForm['content'].value === '') {
                dialog.show({
                    title: '경고', content: '답글을 입력해 주세요.', buttons: [dialog.createButton('확인', function () {
                        dialog.hide();
                        replyForm['content'].focus();
                    })]
                });
                return false;
            }
            if (!replyForm['content'].testRegex()) {
                dialog.show({
                    title: '경고', content: '올바른 답글을 입력해 주세요.', buttons: [dialog.createButton('확인', function () {
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
                            content: '알 수 없는 이유로 답글을 작성하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
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
    }

    const upVote = commentEl.querySelector('[rel="vote"][data-vote="up"]');
    const downVote = commentEl.querySelector('[rel="vote"][data-vote="down"]');
    if (upVote && downVote) {
        downVote.onclick = function () {
            if (downVote.classList.contains('selected')) {
                comment.alterLike(targetComment['index'], null);
            } else {
                comment.alterLike(targetComment['index'], false);
            }
        }
        upVote.onclick = function () {
            if (upVote.classList.contains('selected')) {
                comment.alterLike(targetComment['index'], null);
            } else {
                comment.alterLike(targetComment['index'], true);
            }
        }
    }

    const deleteEl = commentEl.querySelector('[rel="delete"]');
    if (deleteEl) {
        deleteEl.onclick = function () {
            dialog.show({
                title: '댓글 삭제',
                content: '정말로 댓글을 삭제할까요?<br><br>댓글에 답글이 있다면 함께 삭제되니 유의해 주세요.',
                buttons: [dialog.createButton('취소', dialog.hide), dialog.createButton('삭제', function () {
                    dialog.hide();
                    const xhr = new XMLHttpRequest();
                    const formData = new FormData();
                    formData.append('index', targetComment['index']);
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
                                    content: '알 수 없는 이유로 댓글을 삭제하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
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
                    xhr.open('DELETE', './comment');
                    xhr.send(formData);
                    loading.show();
                })]
            });
        }
    }
    const modifyEl = commentEl.querySelector('[rel="modify"]');
    const modifyCancelEl = commentEl.querySelector('[rel="modifyCancel"]');
    if (modifyEl && modifyCancelEl) {
        const modifyForm = commentEl.querySelector('[rel="modifyForm"]');
        modifyEl.onclick = function () {
            commentEl.classList.add('modifying');
            modifyForm['content'].value = commentEl.querySelector('[rel="content"]').innerText;
            modifyForm['content'].focus();
        }
        modifyCancelEl.onclick = function () {
            commentEl.classList.remove('modifying')
        }
        modifyForm.onsubmit = function (e) {
            e.preventDefault();
            console.log(modifyForm);
            if (modifyForm['content'].value === '') {
                dialog.show({
                    title: '경고',
                    content: '댓글을 입력해 주세요.',
                    buttons: [dialog.createButton('확인', function () {
                        dialog.hide();
                        modifyForm['content'].focus();
                    })]
                });
                return false;
            }
            if (!modifyForm['content'].testRegex()) {
                dialog.show({
                    title: '경고',
                    content: '올바른 댓글을 입력해 주세요.',
                    buttons: [dialog.createButton('확인', function () {
                        dialog.hide();
                        modifyForm['content'].focus();
                        modifyForm['content'].select();
                    })]
                });
                return false;
            }
            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('index', targetComment['index']);
            formData.append('content', modifyForm['content'].value);
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
                        commentEl.querySelector('[rel="content"]').innerText = modifyForm['content'].value;
                        commentEl.classList.remove('modifying');
                        break;
                    default:
                        dialog.show({
                            title: '오류',
                            content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                            buttons: [dialog.createButton('확인', dialog.hide)]
                        });
                }
            }

            xhr.open('PATCH', './comment');
            xhr.send(formData);
            loading.show();
        }
    }

    const tbody = commentTable.querySelector(':scope > tbody');
    const tr = document.createElement('tr');
    const td = document.createElement('td');
    td.append(commentEl);
    tr.append(td);
    tbody.append(tr);

    const subComments = allComments.filter(x => x['commentIndex'] === targetComment['index']);
    if (subComments.length > 0) {
        for (const subComment of subComments) {
            comment.append(allComments, subComment, Math.min(step + 1, 4));
        }
    }
}

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
                title: '경고', content: '댓글을 입력해 주세요.', buttons: [dialog.createButton('확인', function () {
                    dialog.hide();
                    commentForm['content'].focus();
                })]
            });
            return false;
        }
        if (!commentForm['content'].testRegex()) {
            dialog.show({
                title: '경고', content: '올바른 댓글을 입력해 주세요.', buttons: [dialog.createButton('확인', function () {
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
comment.load();
