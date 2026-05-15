package com.smartnursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnursing.common.enums.FileTypeEnum;
import com.smartnursing.entity.DigitalPlayLog;
import com.smartnursing.entity.DigitalResource;
import com.smartnursing.entity.FileInfo;
import com.smartnursing.mapper.DigitalPlayLogMapper;
import com.smartnursing.mapper.DigitalResourceMapper;
import com.smartnursing.mapper.FileInfoMapper;
import com.smartnursing.service.DigitalService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class DigitalServiceImpl extends ServiceImpl<DigitalResourceMapper, DigitalResource> implements DigitalService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private DigitalResourceMapper digitalResourceMapper;

    @Autowired
    private DigitalPlayLogMapper digitalPlayLogMapper;

    @Autowired
    private FileInfoMapper fileInfoMapper;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadMedia(MultipartFile file, Long userId)throws Exception{
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }

        String originalFilename= file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = UUID.randomUUID().toString() + extension;

        // 2. 获取文件输入流和大小
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("smart-nursing-bucket")
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        //保存到fileinfo

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(objectName);
        fileInfo.setFileOriginalName(originalFilename);
        fileInfo.setFileSuffix(extension);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setBucket("smart-nursing-bucket");
        // 构造文件的访问 URL (假设 MinIO 是公开可读的，或者你后续会生成预签名 URL)
        fileInfo.setFileUrl("http://localhost:9000/smart-nursing-bucket/" + objectName);
        fileInfo.setFileType(2);
        fileInfo.setUploadUserId(userId);
        fileInfo.setUploadTime(new Date());

        if (fileInfoMapper.insert(fileInfo) <= 0) {
            throw new RuntimeException("文件信息保存失败");
        }

        //保存到digitalresource
        DigitalResource resource = new DigitalResource();
        resource.setResourceName(originalFilename);
        resource.setMediaType(detectMediaType(extension));
        resource.setFileId(fileInfo.getId());
        resource.setStatus(1);
        resource.setVisitCount(0L);
        resource.setCreateUserId(userId);
        resource.setCreateTime(LocalDateTime.now());

        if (digitalResourceMapper.insert(resource) <= 0) {
            throw new RuntimeException("数字资源记录保存失败");
        }

        return String.valueOf(resource.getId());

    }

    private Integer detectMediaType(String extension) {
        if (extension == null) {
            return FileTypeEnum.DOCUMENT.getCode(); // 默认归为文档/其他
        }

        String ext = extension.toLowerCase();

        if (ext.matches("\\.(mp4|avi|mov|wmv|flv|mkv|webm)$")) {
            return FileTypeEnum.VIDEO.getCode();
        }
        else if (ext.matches("\\.(mp3|wav|aac|flac|m4a)$")) {
            return FileTypeEnum.AUDIO.getCode();
        }
        else if (ext.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
            return FileTypeEnum.IMAGE.getCode();
        }
        else {
            return FileTypeEnum.DOCUMENT.getCode();
        }
    }

    @Override
    public String getFileObjectNameByResourceId(Long resourceId, Long userId) {
        //userId没有用到，等待未来扩展权限校验
        DigitalResource digitalResource = digitalResourceMapper.selectById(resourceId);
        if (digitalResource == null) {
            throw new RuntimeException("资源不存在");
        }
        Long fileId = digitalResource.getFileId();
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }
        return fileInfo.getFileName();  // 这个是 MinIO 中的 objectName
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean play(Long resourceId, Long userId) {
        //业务埋点方法。前端在开始播放（或用户点击播放按钮）时，应额外调用一次 /play?resourceId=xxx 接口，通知后端增加播放次数、记录日志。这样播放量的统计就和实际播放行为分离，互不影响。
        //只统计，不处理播放
        DigitalResource digitalResource = digitalResourceMapper.selectById(resourceId);
        if (digitalResource == null) {
            return false;
        }
        // 更新播放次数（使用 LambdaUpdateWrapper）
        LambdaUpdateWrapper<DigitalResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DigitalResource::getId, resourceId)
                .setSql("visit_count = visit_count + 1");
        digitalResourceMapper.update(null, updateWrapper);

        // 插入播放日志
        DigitalPlayLog log = new DigitalPlayLog();
        log.setResourceId(resourceId);
        log.setUserId(userId);
        log.setPlayTime(LocalDateTime.now());  // 或 new Date()
        digitalPlayLogMapper.insert(log);
        return true;
    }

    @Override
    public String createShareUrl(Long resourceId, Long userId) {

        play(resourceId, userId);
        String objectName = getFileObjectNameByResourceId(resourceId, userId);

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .expiry(5, TimeUnit.MINUTES)
                            .bucket("smart-nursing-bucket")
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("生成分享链接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DigitalResource getResourceDetail(Long resourceId) {
        DigitalResource resource = digitalResourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }
        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResource(Long resourceId,Long userId){
        DigitalResource resource=digitalResourceMapper.selectById(resourceId);
        if (resource == null) {
            return false;
        }
        if (!resource.getCreateUserId().equals(userId)) {
            throw new RuntimeException("无权删除该资源");
        }

        FileInfo fileInfo = fileInfoMapper.selectById(resource.getFileId());
        if (fileInfo != null) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket("smart-nursing-bucket")
                                .object(fileInfo.getFileName())
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("删除 MinIO 文件失败: " + e.getMessage(), e);
            }
            fileInfoMapper.deleteById(fileInfo.getId());
        }

        LambdaQueryWrapper<DigitalPlayLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(DigitalPlayLog::getResourceId, resourceId);
        digitalPlayLogMapper.delete(logWrapper);

        digitalResourceMapper.deleteById(resourceId);
        return true;

    }

    @Override
    public boolean updateResource(DigitalResource resource) {
        Long resourceId = resource.getId();
        DigitalResource originaldigitalResource = digitalResourceMapper.selectById(resourceId);
        if (null==originaldigitalResource){
            return false;
        }
        int affectedrows = digitalResourceMapper.updateById(resource);
        if (affectedrows==0){
            return false;
        }
        return true;
    }

    @Override
    public Page<DigitalResource> getResourcePage(int pageNum, int pageSize, String resourceName, Integer mediaType, Integer status) {
        Page<DigitalResource> page=new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DigitalResource> querywrapper=new LambdaQueryWrapper<>();
        querywrapper.like(resourceName!=null,DigitalResource::getResourceName,resourceName)
                .eq(mediaType!=null,DigitalResource::getMediaType,mediaType)
                .eq(status!=null,DigitalResource::getStatus,status)
                .orderByDesc(DigitalResource::getCreateTime);

        digitalResourceMapper.selectPage(page,querywrapper);
        return page;
    }

    @Override
    public boolean updateStatus(Long resourceId, Integer status) {
        DigitalResource digitalResource=new DigitalResource();
        digitalResource.setStatus(status);
        digitalResource.setId(resourceId);

        int affectedrows = digitalResourceMapper.updateById(digitalResource);
        if(affectedrows==0){
            return false;
        }
        return true;

    }


    @Override
    public Page<DigitalPlayLog> getPlayLogPage(int pageNum, int pageSize, Long userId, Long resourceId) {
        Page<DigitalPlayLog> page=new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<DigitalPlayLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, DigitalPlayLog::getUserId, userId)
                .eq(resourceId != null, DigitalPlayLog::getResourceId, resourceId)

                .orderByDesc(DigitalPlayLog::getPlayTime);

        digitalPlayLogMapper.selectPage(page, queryWrapper);

        return page;

    }

    @Override
    public Map<Integer, Long> getPlayCountByMediaType() {
        List<DigitalResource> digitalResources = digitalResourceMapper.selectList(null);
        Map<Integer, Long> playcount =  new HashMap<>();
        for ( DigitalResource digitalResource : digitalResources) {
            Integer mediaType = digitalResource.getMediaType();
            Long visitCount = digitalResource.getVisitCount();

            playcount.compute(mediaType, (key, oldValue) ->
                    oldValue == null ? visitCount : oldValue + visitCount
            );
        }
        return playcount;
    }


}
