package com.yhp.studybbs.mappers;

import com.yhp.studybbs.entities.ContactCompanyEntity;
import com.yhp.studybbs.entities.EmailAuthEntity;
import com.yhp.studybbs.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertEmailAuth(EmailAuthEntity emailAuth);

    ContactCompanyEntity[] selectContactCompanies();

    EmailAuthEntity selectEmailAuthByEmailCodeSalt(@Param(value = "email") String email, //@Param의 value의 값인 email이 UserMapper.xml의 #{email} 과 같아야한다.
                                                   @Param(value = "code") String code,
                                                   @Param(value = "salt") String salt);

    UserEntity selectUserByEmail(@Param(value = "email") String email);

    int updateEmailAuth(EmailAuthEntity emailAuth);
}