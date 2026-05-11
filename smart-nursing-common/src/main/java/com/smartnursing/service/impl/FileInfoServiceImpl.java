package com.smartnursing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnursing.entity.FileInfo;
import com.smartnursing.service.FileInfoService;
import com.smartnursing.mapper.FileInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author 33207
* @description 针对表【file_info(文件信息)】的数据库操作Service实现
* @createDate 2026-05-08 09:19:08
*/
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
    implements FileInfoService{

}




