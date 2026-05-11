package com.smartnursing.mapper;

import com.smartnursing.entity.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 33207
* @description 针对表【file_info(文件信息)】的数据库操作Mapper
* @createDate 2026-05-08 09:19:08
* @Entity .entity.FileInfo
*/
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

}




