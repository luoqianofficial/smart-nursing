package com.smartnursing.mapper;



import com.baomidou.mybatisplus.annotation.TableName;
import com.smartnursing.entity.SysRolePerm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 33207
* @description 针对表【sys_role_perm(角色权限关联)】的数据库操作Mapper
* @createDate 2026-05-08 09:19:08
* @Entity .entity.SysRolePerm
*/
@Mapper
public interface SysRolePermMapper extends BaseMapper<SysRolePerm> {

}




