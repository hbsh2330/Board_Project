package com.yhp.studybbs.interceptors;

import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.services.BoardService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonInterceptor implements HandlerInterceptor {
    @Resource
    private BoardService boardService;
    @Override //true 면 컨트롤러에 request가 가는걸 허용함 false면 컨트롤러에 가는것을 허락하지 않음
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equalsIgnoreCase("GET") && !request.getMethod().equalsIgnoreCase("POST")){
            return true;
        }
        BoardEntity[] boards = this.boardService.getBoards();
        request.setAttribute("boards", boards); // fragments의 boards와 매핑
        return true;
    }
}
