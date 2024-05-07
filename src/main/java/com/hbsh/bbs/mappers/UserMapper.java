package com.hbsh.bbs.mappers;

import com.hbsh.bbs.entities.ContactCompanyEntity;
import com.hbsh.bbs.entities.EmailAuthEntity;
import com.hbsh.bbs.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertEmailAuth(EmailAuthEntity emailAuth); //insert, Delete, update는 int형식

    int insertUser(UserEntity user);

    ContactCompanyEntity[] selectContactCompanies();

    EmailAuthEntity selectEmailAuthByEmailCodeSalt(@Param(value = "email") String email,
                                                   @Param(value = "code") String code,
                                                   @Param(value = "salt") String salt);

    UserEntity selectUserByContact(@Param(value = "contactFirst") String contact,
                                   @Param(value = "contactSecond") String contactSecond,
                                   @Param(value = "contactThird") String contactThird);
//@Param의 value= "contactFirst"이 mapper.xml의 #{contactFirst}과 동일해야함!!!! 중요!!!
    UserEntity selectUserByEmail(@Param(value = "email") String email);

    UserEntity selectUserByNickname(@Param(value = "nickname") String nickname);

    int updateEmailAuth(EmailAuthEntity emailAuth);

    int updateUser(UserEntity user);

}