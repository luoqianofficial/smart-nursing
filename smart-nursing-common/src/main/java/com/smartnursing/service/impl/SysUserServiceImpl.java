package com.smartnursing.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnursing.entity.SysUser;
import com.smartnursing.service.SysUserService;
import com.smartnursing.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 33207
* @description 针对表【sys_user(系统用户)】的数据库操作Service实现
* @createDate 2026-05-08 09:19:08
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




