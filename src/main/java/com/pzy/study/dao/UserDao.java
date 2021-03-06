package com.pzy.study.dao;

import com.pzy.study.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Destription:
 * Author: pengzuyao
 * Time: 2019-05-19
 */
@Mapper
public interface UserDao {

    UserEntity findUserInfoByName(String username);

    List<UserEntity> selectUsersByDeptId(Integer deptId);

    void addUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity);

    List<UserEntity> selectByUserIds(@Param("userIds")List<Integer> userIds);

    List<UserEntity> selectAll();

    void deleteRoleUserRelByRoleId(Integer roleId);
}
