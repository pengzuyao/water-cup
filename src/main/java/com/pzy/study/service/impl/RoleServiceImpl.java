package com.pzy.study.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pzy.study.base.commons.utils.IpAddressUtil;
import com.pzy.study.base.commons.utils.RequestHolder;
import com.pzy.study.dao.AclDao;
import com.pzy.study.dao.RoleDao;
import com.pzy.study.dao.RoleUserRelDao;
import com.pzy.study.dao.UserDao;
import com.pzy.study.entity.*;
import com.pzy.study.service.AclModuleService;
import com.pzy.study.service.RoleService;
import com.pzy.study.vo.RoleVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Destription:
 * Author: pengzuyao
 * Time: 2019-06-03
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private AclDao aclDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleUserRelDao roleUserRelDao;

    @Autowired
    private AclModuleService aclModuleService;

    @Override
    public void insertRole(RoleVo roleVo) {
        RoleEntity build = RoleEntity.builder().
                name(roleVo.getName()).
                remark(roleVo.getRemark()).
                status(roleVo.getStatus()).
                type(1).
                operator(RequestHolder.getCurrentUser().getUsername()).
                operatorIp(IpAddressUtil.getIpAdrress(RequestHolder.getCurrentRequest())).
                build();
        roleDao.insertRole(build);
    }

    @Override
    public void updateRole(RoleVo roleVo) {
        RoleEntity build = RoleEntity.builder().
                id(roleVo.getId()).
                name(roleVo.getName()).
                remark(roleVo.getRemark()).
                status(roleVo.getStatus()).
                type(1).
                operator(RequestHolder.getCurrentUser().getUsername()).
                operatorIp(IpAddressUtil.getIpAdrress(RequestHolder.getCurrentRequest())).
                build();
        roleDao.updateRole(build);
    }

    @Override
    public List<RoleEntity> getAll() {
        return roleDao.getAll();
    }

    @Override
    public List<AclModuleLevelEntity> roleAclTree(Integer roleId){
        //1、当前用户已分配的权限点
        List<RoleAclEntity> userAcls = getCurrentUserAclList();
        //2、当前角色分配的权限点
        List<AclEntity> roleAcls = aclDao.selectAclsByRoles(Collections.singletonList(roleId));
        List<Integer> userAclIds = userAcls.stream().map(roleAclEntity -> roleAclEntity.getId()).collect(Collectors.toList());
        List<Integer> roleAclIds = roleAcls.stream().map(roleAclEntity -> roleAclEntity.getId()).collect(Collectors.toList());
        //当前系统所有权限点
        List<AclEntity> collect = aclDao.selectAll();
        List<RoleAclEntity> roleAclRelEntities = Lists.newArrayList();
        collect.forEach(roleAclRelEntity -> {
            RoleAclEntity adapt = RoleAclEntity.adapt(roleAclRelEntity);
            if (userAclIds.contains(adapt.getId())){adapt.setHasAcl(true);}
            if (roleAclIds.contains(adapt.getId())){adapt.setChecked(true);}
            roleAclRelEntities.add(adapt);
        });
        return aclListToTree(roleAclRelEntities);

    }

    List<AclModuleLevelEntity>  aclListToTree(List<RoleAclEntity> aclList){
        if (aclList.isEmpty()){return Lists.newArrayList();}
        List<AclModuleLevelEntity> aclModuleEntities = aclModuleService.getAclModuleTree();
        Map<Integer, List<RoleAclEntity>> aclModuleIdAclMap = aclList.stream().collect(Collectors.groupingBy(roleAclRelEntity -> roleAclRelEntity.getAclModuleId()));
        bindAclWithOrder(aclModuleEntities , aclModuleIdAclMap);
        return aclModuleEntities;
    }

    private void bindAclWithOrder(List<AclModuleLevelEntity> aclModuleEntities, Map<Integer, List<RoleAclEntity>> aclModuleIdAclMap) {
            if (aclModuleEntities.isEmpty()){
                return;
            }
        for (AclModuleLevelEntity aclModuleEntity : aclModuleEntities) {
            List<RoleAclEntity> roleAclRelEntities = aclModuleIdAclMap.get(aclModuleEntity.getId());
            if (CollectionUtils.isNotEmpty(roleAclRelEntities)){
                Collections.sort(roleAclRelEntities, new Comparator<RoleAclEntity>() {
                    @Override
                    public int compare(RoleAclEntity o1, RoleAclEntity o2) {
                        return o1.getSeq() - o2.getSeq();
                    }
                });
            }
            aclModuleEntity.setAclList(roleAclRelEntities);
            bindAclWithOrder(aclModuleEntity.getAclModuleList() , aclModuleIdAclMap);
        }

    }


    @Override
    public List<RoleAclEntity> getCurrentUserAclList() {
        Integer userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    public List<RoleAclEntity> getUserAclList(Integer userId){
        List<RoleAclEntity> roleAclRelEntities = Lists.newArrayList();
        if (isSuperAdmin()){
            List<AclEntity> aclEntities = aclDao.selectAll();
            adapt(roleAclRelEntities ,aclEntities);
            return roleAclRelEntities;
        }
        List<Integer> roles = roleUserRelDao.selectRoleIdsByUserId(userId);
        if (roles.isEmpty()){
            return Lists.newArrayList();
        }
        List<AclEntity> acls = aclDao.selectAclsByRoles(roles);
        adapt(roleAclRelEntities , acls);
        return roleAclRelEntities;
    }

    public void adapt(List<RoleAclEntity> roleAclRelEntities , List<AclEntity> aclEntities){
        aclEntities.forEach(aclEntity -> {
            RoleAclEntity adapt = RoleAclEntity.adapt(aclEntity);
            roleAclRelEntities.add(adapt);
        });
    }

    private boolean isSuperAdmin() {
        return true;
    }

    @Override
    public Map<String, List<UserEntity>> roleUsers(Integer roleId) {
        List<UserEntity> allUsers = userDao.selectAll();
        List<Integer> userIds = roleUserRelDao.selectUserIdsByRoleId(roleId);
        List<UserEntity> selectUsers = Lists.newArrayList();
        Map<String, List<UserEntity>> map = Maps.newHashMap();
        if (CollectionUtils.isEmpty(userIds)){
            map.put("selected" , selectUsers);
            map.put("unselected" , allUsers);
            return map;
        }
        selectUsers = userDao.selectByUserIds(userIds);
        List<Integer> selectUserIds = selectUsers.stream().map(userEntity -> userEntity.getId()).collect(Collectors.toList());
        List<UserEntity> unSelectUsers = allUsers.stream().filter(userEntity -> !selectUserIds.contains(userEntity.getId())).collect(Collectors.toList());
        map.put("selected" , selectUsers);
        map.put("unselected" , unSelectUsers);
        return map;
    }

    @Override
    public List<RoleEntity> findRolesByUserId(Integer userId) {
        List<Integer> list = roleUserRelDao.selectRoleIdsByUserId(userId);
        if (list.isEmpty()){
            return Lists.newArrayList();
        }
        return roleDao.getRoleListByRoleIds(list);
    }
}
