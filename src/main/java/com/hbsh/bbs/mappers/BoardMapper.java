package com.hbsh.bbs.mappers;

import com.hbsh.bbs.dtos.ArticleDto;
import com.hbsh.bbs.entities.BoardEntity;
import com.hbsh.bbs.vos.PageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {

    BoardEntity selectBoardByCode(@Param(value = "code")String code);
    BoardEntity[] selectBoards();

    ArticleDto[] selectArticleDtosByPage(@Param(value = "board") BoardEntity board,
                                         @Param(value = "page") PageVo page);

    int selectArticleCountByBoard(BoardEntity board);

}
