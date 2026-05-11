package com.smartnursing.mapper;

import com.smartnursing.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 33207
* @description 针对表【sys_permission(权限)】的数据库操作Mapper
* @createDate 2026-05-08 09:19:08
* @Entity .entity.SysPermission
*/
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    public List<String> listPermCodeByUserId(@Param("userId")Long userid);
}




