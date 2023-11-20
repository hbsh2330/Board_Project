package com.yhp.studybbs.mappers;

import com.yhp.studybbs.entities.BoardEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {

    BoardEntity selectBoardByCode(@Param(value = "code")String code);
    BoardEntity[] selectBoards();

}
