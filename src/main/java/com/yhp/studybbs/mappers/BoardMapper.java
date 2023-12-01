package com.yhp.studybbs.mappers;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.vos.PageVo;
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
