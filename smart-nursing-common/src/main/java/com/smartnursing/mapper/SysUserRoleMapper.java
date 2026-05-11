package com.smartnursing.mapper;

import com.smartnursing.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 33207
* @description 针对表【sys_user_role(用户-角色关联表)】的数据库操作Mapper
* @createDate 2026-05-08 10:26:11
* @Entity .entity.SysUserRole
*/
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}




