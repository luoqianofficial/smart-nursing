package com.smartnursing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnursing.entity.DigitalPlayLog;
import com.smartnursing.entity.DigitalResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DigitalService extends IService<DigitalResource> {

    // 上传多媒体（分片/断点续传）
    String uploadMedia(MultipartFile file, Long userId)throws Exception;

    String getFileObjectNameByResourceId(Long resourceId, Long userId);

    // 播放
    boolean play(Long resourceId, Long userId);

    // 生成分享链接
    // 生成分享/播放链接时，同时记录播放日志
    String createShareUrl(Long resourceId, Long userId);

    DigitalResource getResourceDetail(Long resourceId);

    boolean deleteResource(Long resourceId, Long userId);

    boolean updateResource(DigitalResource resource);
    // 编辑资源信息

    Page<DigitalResource> getResourcePage(int pageNum, int pageSize, String resourceName, Integer mediaType, Integer status);
    // 分页查询，支持条件筛选

    boolean updateStatus(Long resourceId, Integer status);
    // 上架/下架

    Page<DigitalPlayLog> getPlayLogPage(int pageNum, int pageSize, Long userId, Long resourceId);
    // 播放日志分页查询

    Map<Integer, Long> getPlayCountByMediaType();
    // 按媒体类型统计播放量
}
