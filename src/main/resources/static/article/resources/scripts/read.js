// const commentTable = document.getElementById('commentTable');
//
// const comment = {
//     load: function (){
//         const xhr = new XMLHttpRequest();
//         xhr.onreadystatechange = function(){
//             if(xhr.readyState !== XMLHttpRequest.DONE){
//                 return;
//             }
//             commentTable.classList.remove('loading')
//             if(xhr.status < 200 || xhr.status >= 300){
//
//                 return;
//             }
//
//         }
//         xhr.open('GET', `./comment?articleIndex=${commentForm['articleIndex'].value}`);
//         xhr.send();
//         commentTable.classList.add('loading');
//     }
// };
//
// const commentForm = document.getElementById('commentForm');
//
// if (commentForm) {
//     commentForm.onsubmit = function (e) {
//         e.preventDefault();
//
//         if (commentForm['content'].value === ''){}
//     }
//     if (!commentForm['content'].testRegex()){
//         dialog.show({
//             title: '경고',
//             content: '올바른 댓글을 입력해주세요',
//             buttons: [dialog.createButton('확인', function (){
//                 dialog.hide();
//                 commentForm['content']
//             })]
//         })
//         const xhr = new XMLHttpRequest();
//         const formData = new FormData();
//         formData.append('articleIndex', commentForm['articleIndex'].value)
//         formData.append('content', c)
//         xhr.onreadystatechange = function(){
//             if(xhr.readyState !== XMLHttpRequest.DONE){
//                 return;
//             }
//             if(xhr.status < 200 || xhr.status >= 300){
//
//                 case 'success':
//                     commentForm['content'].value = '';
//                     commentForm['content'].focus();
//                     comment.load();
//                 return;
//             }
//
//         }
//         xhr.open('POST', './comment');
//         xhr.send(formData);
//         loading.show()
//     }
// }