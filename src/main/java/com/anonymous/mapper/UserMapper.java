package com.anonymous.mapper;

import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.model.User;
import com.anonymous.vo.admin.UserAdminVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Update("UPDATE user SET name = #{name}, update_time = NOW() WHERE id = #{id}")
    int updateNameById(@Param("id") Long id, @Param("name") String name);

    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    @Insert("INSERT INTO user (name, username, password) VALUES (#{name}, #{username}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(@Param("name") String name, @Param("username") String username, @Param("password") String password);

    List<UserAdminVO> findUsersByCondition(@Param("query") UserQueryDTO queryDTO,
                                           @Param("offset") Integer offset,
                                           @Param("size") Integer size);

    Long countUsersByCondition(@Param("query") UserQueryDTO queryDTO);

    UserAdminVO findUserById(@Param("id") Long id);

    int updateUser(@Param("query") UserUpdateDTO queryDTO);
}
