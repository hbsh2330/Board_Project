package com.yhp.studybbs.mappers;

import com.yhp.studybbs.entities.BoardEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {
    BoardEntity[] selectBoards();

}
