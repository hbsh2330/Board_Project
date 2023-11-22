package com.yhp.studybbs.controllers;

import com.yhp.studybbs.entities.*;
import com.yhp.studybbs.results.article.UploadFileResult;
import com.yhp.studybbs.results.article.UploadImageResult;
import com.yhp.studybbs.results.article.WriteResult;
import com.yhp.studybbs.services.ArticleService;
import com.yhp.studybbs.services.BoardService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

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
    public ModelAndView getWrite(@SessionAttribute(value = "user", required = false)UserEntity user, @RequestAttribute(value = "boards"
    ) BoardEntity[] boards, @RequestParam(value = "code", required = false, defaultValue = "")String code){
        ModelAndView modelAndView = new ModelAndView();
        if (user == null) { //로그인을 하지않으면
            modelAndView.setViewName("redirect:/user/login"); //로그인 페이지로 리다이렉트
        } else {
            BoardEntity board = null; //
            for (BoardEntity b: boards){
                if (b.getCode().equals(code)){ //board에 있는 code값과 requestParam으로 받아온 code값이 같을경우
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
        if (fileIndexes == null){
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
    public String postImage(@SessionAttribute(value = "user")UserEntity user,
                            @RequestParam(value = "upload")MultipartFile file) throws IOException {
        ImageEntity image = new ImageEntity(file);
        UploadImageResult result = this.articleService.uploadImage(image, user);
        JSONObject responseObject = new JSONObject();
        if (result == UploadImageResult.SUCCESS){
            responseObject.put("url", "/article/image?index=" + image.getIndex());
        } else {
            JSONObject messageObject = new JSONObject();
            messageObject.put("message", "알 수 없는 이유로 이미지를 업로드 하지 못하였습니다.");
            responseObject.put("error", messageObject);
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "index") int index){
        ResponseEntity<byte[]> response;
        ImageEntity image = this.articleService.getImage(index);
        if(image == null){
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

    @RequestMapping(value = "file", //이미지를 올리기위한 컨트롤러
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postFile(@SessionAttribute(value = "user")UserEntity user,
                            @RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        FileEntity file = new FileEntity(multipartFile);
        UploadFileResult result = this.articleService.uploadFile(file, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        if (result == UploadFileResult.SUCCESS){
            responseObject.put("index", file.getIndex());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "read",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRead(@RequestParam(value = "index")int index){
        ModelAndView modelAndView = new ModelAndView();
        ArticleEntity article = this.articleService.getArticle(index);
        if (article != null && !article.isDeleted()){
            FileEntity[] files = this.articleService.getFilesOf(article);
            modelAndView.addObject("files", files);
        }
        modelAndView.addObject("article", article);
        modelAndView.setViewName("article/read");
        return modelAndView;
    }

}
