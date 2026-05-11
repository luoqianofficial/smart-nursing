package com.smartnursing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnursing.entity.DigitalResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public interface DigitalService extends IService<DigitalResource> {

    // 上传多媒体（分片/断点续传）
    String uploadMedia(MultipartFile file, Long userId)throws Exception;

    String getFileObjectNameByResourceId(Long resourceId, Long userId);

    // 播放
    boolean play(Long resourceId, Long userId);

    // 生成分享链接
    String createShareUrl(Long resourceId, Long userId);

    DigitalResource getResourceDetail(Long resourceId);

    boolean deleteResource(Long resourceId, Long userId);
}
