package com.smartnursing.smartnursingadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartnursing.entity.SysUser;
import com.smartnursing.mapper.SysPermissionMapper;
import com.smartnursing.mapper.SysUserMapper;
import com.smartnursing.smartnursingadmin.security.LoginUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SysUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户

        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserMapper.selectOne(wrapper);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 查询权限,返回真正的权限列表
        List<String> permissions = sysPermissionMapper.listPermCodeByUserId(sysUser.getId());
        //防止空指针
        if(null==permissions){
            permissions= Collections.emptyList();
        }


        // 3. 通过构造方法传入数据
        return new LoginUserDetails(sysUser, permissions);
    }
}
