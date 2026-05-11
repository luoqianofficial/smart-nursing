package com.smartnursing.mapper;

import com.smartnursing.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 33207
* @description 针对表【sys_role(角色)】的数据库操作Mapper
* @createDate 2026-05-08 09:19:08
* @Entity .entity.SysRole
*/
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> listRolesByUserId(@Param("userId") Long userId);
}




