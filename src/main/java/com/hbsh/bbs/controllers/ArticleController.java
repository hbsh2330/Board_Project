package com.hbsh.bbs.controllers;

import com.hbsh.bbs.dtos.ArticleDto;
import com.hbsh.bbs.dtos.CommentDto;
import com.hbsh.bbs.entities.*;
import com.hbsh.bbs.results.article.*;
import com.hbsh.bbs.services.ArticleService;
import com.hbsh.bbs.services.BoardService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@Controller
@RequestMapping(value = "article")
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @Autowired
    public ArticleController(BoardService boardService, ArticleService articleService) {
        this.boardService = boardService;
        this.articleService = articleService;
    }

    @RequestMapping(value = "write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite(@SessionAttribute(value = "user", required = false) UserEntity user, @RequestAttribute(value = "boards"
    ) BoardEntity[] boards, @RequestParam(value = "code", required = false, defaultValue = "") String code) {
        ModelAndView modelAndView = new ModelAndView();
        if (user == null) { //로그인을 하지않으면
            modelAndView.setViewName("redirect:/user/login"); //로그인 페이지로 리다이렉트
        } else {
            BoardEntity board = null; //
            for (BoardEntity b : boards) {
                if (b.getCode().equals(code)) { //board에 있는 code값과 requestParam으로 받아온 code값이 같을경우
                    board = b; //board에 borad배열의 값을 집어 넣는다.
                    break;
                }
            }
            boolean allowed = board != null && (!board.isAdminWrite() || user.isAdmin()); //board에 파라미터로 입력한 코드의 값과 내부에 있는 코드의 값이 같거나, 관리자 전용쓰기 일경우, 관리자가  아닐경우 true를 반환
            modelAndView.addObject("board", board);
            modelAndView.addObject("allowed", allowed);
            modelAndView.setViewName("/article/write");
        }
        return modelAndView;
    }

    @RequestMapping(value = "write", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postWrite(@SessionAttribute(value = "user") UserEntity user,
                            @RequestParam(value = "fileIndexes", required = false) int[] fileIndexes,
                            ArticleEntity article) {
        if (fileIndexes == null) {
            fileIndexes = new int[0];
        }
        WriteResult result = this.articleService.write(article, fileIndexes, user);
        JSONObject resultObject = new JSONObject();
        resultObject.put("result", result.name().toLowerCase());
        if (result == WriteResult.SUCCESS) {
            resultObject.put("index", article.getIndex());
        }
        return resultObject.toString();
    }

    @RequestMapping(value = "image", //이미지를 올리기위한 컨트롤러
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postImage(@SessionAttribute(value = "user") UserEntity user,
                            @RequestParam(value = "upload") MultipartFile file) throws IOException {
        ImageEntity image = new ImageEntity(file);
        UploadImageResult result = this.articleService.uploadImage(image, user);
        JSONObject responseObject = new JSONObject();
        if (result == UploadImageResult.SUCCESS) {
            responseObject.put("url", "/article/image?index=" + image.getIndex());
        } else {
            JSONObject messageObject = new JSONObject();
            messageObject.put("message", "알 수 없는 이유로 이미지를 업로드 하지 못하였습니다.");
            responseObject.put("error", messageObject);
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "index") int index) {
        ResponseEntity<byte[]> response;
        ImageEntity image = this.articleService.getImage(index);
        if (image == null) {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            // body, header, status
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(image.getType()));
            httpHeaders.setContentLength(image.getSize());
            response = new ResponseEntity<>(image.getData(), httpHeaders, HttpStatus.OK);
        }
        return response;
    }

    @RequestMapping(value = "file", //파일을 올리기위한 컨트롤러
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postFile(@SessionAttribute(value = "user") UserEntity user,
                           @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        FileEntity file = new FileEntity(multipartFile);
        UploadFileResult result = this.articleService.uploadFile(file, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        if (result == UploadFileResult.SUCCESS) {
            responseObject.put("index", file.getIndex());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "read",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRead(@RequestAttribute(value = "boards") BoardEntity[] boards,
                                @RequestParam(value = "index") int index,
                                @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        ArticleDto article = this.articleService.getArticleDto(index);
        if (article != null && !article.isDeleted()) {
            BoardEntity board = null; //
            for (BoardEntity b : boards) {
                if (b.getCode().equals(article.getBoardCode())) {
                    board = b; //board에 borad배열의 값을 집어 넣는다.
                    break;
                }
            }
            FileEntity[] files = this.articleService.getFilesOf(article);
            modelAndView.addObject("files", files);
            modelAndView.addObject("board", board);
            modelAndView.addObject("page", page);
        }
        modelAndView.addObject("article", article);
        modelAndView.setViewName("article/read");
        return modelAndView;
    }

    @RequestMapping(value = "file", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getFile(@RequestParam(value = "index") int index) {
        ResponseEntity<byte[]> response;
        FileEntity file = this.articleService.getFile(index);
        if (file == null) {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            ContentDisposition contentDisposition = ContentDisposition
                    .attachment() //다운로드가 되고 inline은 표시가 됨 //첨부파일이니 다운로드 해야하니까
                    .filename(file.getName(), StandardCharsets.UTF_8)
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(file.getSize());
            headers.setContentType(MediaType.parseMediaType(file.getType()));
            headers.setContentDisposition(contentDisposition);
            response = new ResponseEntity<>(file.getData(), headers, HttpStatus.OK);
        }
        return response;
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postComment(@SessionAttribute(value = "user") UserEntity user,
                              CommentEntity comment) {
        WriteCommentResult result = this.articleService.writeComment(comment, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getComment(@SessionAttribute(value = "user", required = false) UserEntity user,
                             @RequestParam(value = "articleIndex") int articleIndex) {
        CommentDto[] comments = this.articleService.getCommentDtos(articleIndex, user == null ? null : user.getEmail());
        JSONArray responseArray = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //원하는 DATE형태를 문자열로 만들때 사용
        for (CommentDto comment : comments) {
            JSONObject commentObject = new JSONObject();
            commentObject.put("index", comment.getIndex());
            commentObject.put("articleIndex", comment.getArticleIndex());
            commentObject.put("userEmail", comment.getUserEmail());
            commentObject.put("userNickname", comment.getUserNickname());
            commentObject.put("commentIndex", comment.getCommentIndex());
//            commentObject.put("content", comment.getContent());
            if (comment.getModifiedAt() == null) {
                commentObject.put("at", dateFormat.format(comment.getWrittenAt()));
                commentObject.put("isModified", false); // 수정안했으면 최초 작성일시
            } else {
                commentObject.put("at", dateFormat.format(comment.getModifiedAt()));
                commentObject.put("isModified", true); //수정했으면 최종수정일시
            }
            commentObject.put("isMine", user != null && (user.getEmail().equals(comment.getUserEmail()) || user.isAdmin())); // 내꺼면 수정/삭제 가능여부
            if (!comment.isDeleted()) {
                commentObject.put("content", comment.getContent());
                commentObject.put("likeCount", comment.getLikeCount());
                commentObject.put("likeStatus", comment.getLikeStatus());
                commentObject.put("dislikeCount", comment.getDislikeCount());
            }
            responseArray.put(commentObject);
        }
        return responseArray.toString();
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String putComment(@SessionAttribute(value = "user") UserEntity user,
                             @RequestParam(value = "commentIndex") int commentIndex,
                             @RequestParam(value = "status", required = false) Boolean status) {
        AlterCommentLikeResult result = this.articleService.alterCommentLike(commentIndex, status, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        if (result == AlterCommentLikeResult.SUCCESS) {
            CommentDto comment = this.articleService.getCommentDto(commentIndex, user.getEmail());
            responseObject.put("likeCount", comment.getLikeCount());
            responseObject.put("likeStatus", comment.getLikeStatus());
            responseObject.put("dislikeCount", comment.getDislikeCount());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteComment(@SessionAttribute(value = "user") UserEntity user,
                                @RequestParam(value = "index") int index) {
        DeleteCommentResult result = this.articleService.deleteComment(index, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.PATCH,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchComment(@SessionAttribute(value = "user") UserEntity user,
                               CommentEntity comment) {
        ModifyCommentResult result = this.articleService.modifyComment(comment, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "read",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteRead(@SessionAttribute(value = "user") UserEntity user,
                             @RequestParam(value = "index") int index) {
        DeleteResult result = this.articleService.delete(index, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "modify",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getModify(@SessionAttribute(value = "user") UserEntity user,
                                  @RequestParam(value = "index") int index,
                                  @RequestAttribute(value = "boards"
                                  ) BoardEntity[] boards){
        ModelAndView modelAndView = new ModelAndView();
        ArticleDto article = this.articleService.getArticleDto(index);
        if (!article.getUserEmail().equals(user.getEmail()) && !user.isAdmin()){
            article = null;
        } else {
            final String boardCode = article.getBoardCode();
            BoardEntity board = Arrays.stream(boards).filter(x -> x.getCode().equals(boardCode)).findFirst().orElse(null);
            FileEntity[] files = this.articleService.getFilesOf(article);
            modelAndView.addObject("board", board);
            modelAndView.addObject("files", files);
        }
        modelAndView.setViewName("article/modify");
        modelAndView.addObject("article", article);
        return modelAndView;
    }

    @RequestMapping(value = "modify",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postModify(@SessionAttribute(value = "user") UserEntity user,
                             @RequestParam(value = "fileIndexes") int[] fileIndexes,
                             ArticleEntity article){
        if (fileIndexes == null){
            fileIndexes = new int[0];
        }
        ModifyResult result = this.articleService.modify(article, fileIndexes, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }
}
