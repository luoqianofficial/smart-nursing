package com.smartnursing.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnursing.common.enums.FileTypeEnum;
import com.smartnursing.entity.FileInfo;
import com.smartnursing.entity.KnowledgeDownloadLog;
import com.smartnursing.entity.KnowledgeResource;
import com.smartnursing.knowledge.mapper.KnowledgeDownloadLogMapper;
import com.smartnursing.knowledge.mapper.KnowledgeResourceMapper;
import com.smartnursing.knowledge.service.KnowledgeService;
import com.smartnursing.mapper.FileInfoMapper;
import com.smartnursing.mapper.SysUserMapper;
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
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeResourceMapper, KnowledgeResource> implements KnowledgeService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private KnowledgeResourceMapper knowledgeResourceMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private KnowledgeDownloadLogMapper knowledgeDownloadLogMapper;

    /**
     * 上传知识资源
     *
     * @param file       上传的文件
     * @param userId     上传人ID
     * @param categoryId 分类ID
     * @return 资源ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadResource(MultipartFile file, Long userId, Long categoryId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        List<String> allowedExtensions = Arrays.asList(".pdf", ".doc", ".docx", ".ppt", ".pptx", ".png", ".jpg", ".jpeg");
        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("不支持的文件格式，仅支持: pdf, doc, docx, ppt, pptx, png, jpg, jpeg");
        }

        String objectName = UUID.randomUUID().toString() + extension;

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

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(objectName);
        fileInfo.setFileOriginalName(originalFilename);
        fileInfo.setFileSuffix(extension);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setBucket("smart-nursing-bucket");
        fileInfo.setFileUrl("http://localhost:9000/smart-nursing-bucket/" + objectName);
        fileInfo.setFileType(detectKnowledgeFileType(extension)); // 传入 extension 而不是 objectName
        fileInfo.setUploadUserId(userId);
        fileInfo.setUploadTime(new Date());

        if (fileInfoMapper.insert(fileInfo) <= 0) {
            throw new RuntimeException("文件信息保存失败");
        }

        KnowledgeResource resource = new KnowledgeResource();
        resource.setResourceName(originalFilename);
        resource.setCategoryId(categoryId);
        resource.setFileId(fileInfo.getId());
        resource.setStatus(0);
        resource.setVisitCount(0L);
        resource.setDownloadCount(0L);
        resource.setCreateUserId(userId);
        resource.setCreateTime(LocalDateTime.now());

        if (knowledgeResourceMapper.insert(resource) <= 0) {
            throw new RuntimeException("知识资源记录保存失败");
        }

        return String.valueOf(resource.getId());
    }

    /**
     * 编辑知识资源信息（不修改文件）
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResource(KnowledgeResource resource) {
        Long resourceId = resource.getId();
        KnowledgeResource knowledgeResource = knowledgeResourceMapper.selectById(resourceId);
        if(knowledgeResource==null){
            return false;
        }

        return knowledgeResourceMapper.updateById(resource) > 0;

    }

    /**
     * 删除知识资源（同时删除MinIO文件 + file_info记录 + 下载日志）
     *
     * @param resourceId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResource(Long resourceId, Long userId) {
        KnowledgeResource knowledgeResource = knowledgeResourceMapper.selectById(resourceId);
        if(knowledgeResource==null){
            return false;
        }
        // 使用 Objects.equals 防止空指针，或者确保 createUserId 不为 null
        if (!Objects.equals(knowledgeResource.getCreateUserId(), userId)) {
            throw new RuntimeException("无权删除该资源");
        }
        FileInfo fileInfo = fileInfoMapper.selectById(knowledgeResource.getFileId());
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

        LambdaQueryWrapper<KnowledgeDownloadLog> logWrapper=new LambdaQueryWrapper<>();
        logWrapper.eq(KnowledgeDownloadLog::getResourceId,resourceId);
        // 删除下载日志
        knowledgeDownloadLogMapper.delete(logWrapper);

        // 删除资源主记录
        knowledgeResourceMapper.deleteById(resourceId);
        return true;

    }

    /**
     * 资源列表分页查询（支持按分类、名称筛选）
     *
     * @param pageNum
     * @param pageSize
     * @param categoryId
     * @param resourceName
     * @param status
     */
    @Override
    public IPage<KnowledgeResource> getResourcePage(int pageNum, int pageSize, Long categoryId, String resourceName, Integer status) {
        Page<KnowledgeResource> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<KnowledgeResource> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 按分类ID筛选（如果传了的话）
        queryWrapper.eq(categoryId != null, KnowledgeResource::getCategoryId, categoryId)

                // 2. 按资源名称模糊搜索（如果传了的话）
                .like(resourceName != null && !resourceName.isEmpty(), KnowledgeResource::getResourceName, resourceName)

                // 3. 按状态筛选（如果传了的话，比如只看上架的）
                .eq(status != null, KnowledgeResource::getStatus, status)

                // 4. 排序：通常按创建时间倒序，或者按ID倒序
                .orderByDesc(KnowledgeResource::getCreateTime);

        knowledgeResourceMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 获取资源详情
     *
     * @param resourceId
     */
    @Override
    public KnowledgeResource getResourceDetail(Long resourceId) {
        KnowledgeResource knowledgeResource = knowledgeResourceMapper.selectById(resourceId);
        if (knowledgeResource==null){
            throw  new RuntimeException("资源不存在");
        }
        return knowledgeResource;
    }

    /**
     * 生成下载地址（返回MinIO预签名URL）
     * 每次调用会：download_count + 1，插入下载日志
     *
     * @param resourceId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getDownloadUrl(Long resourceId, Long userId) throws Exception {
        KnowledgeResource knowledgeResource = knowledgeResourceMapper.selectById(resourceId);

        if (knowledgeResource == null || knowledgeResource.getStatus() != 1) {
            throw new RuntimeException("资源不存在或已下架，无法下载");
        }
        // 增加下载次数
        LambdaUpdateWrapper<KnowledgeResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeResource::getId, resourceId)
                .setSql("download_count = download_count + 1");
        knowledgeResourceMapper.update(null, updateWrapper);

        // 插入下载日志 (核心需求：必须记录谁在什么时候下载了什么)
        KnowledgeDownloadLog log = new KnowledgeDownloadLog();
        log.setResourceId(resourceId);
        log.setUserId(userId);
        log.setDownloadTime(LocalDateTime.now());
        knowledgeDownloadLogMapper.insert(log);

        //获取文件信息并生成 URL
        Long fileId = knowledgeResource.getFileId();
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);

        if (fileInfo == null) {
            throw new RuntimeException("关联的文件信息丢失，请联系管理员");
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .expiry(5, TimeUnit.MINUTES)
                            .bucket("smart-nursing-bucket")
                            .object(fileInfo.getFileName())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }


    }

    /**
     * 根据分类ID查询资源列表
     *
     * @param categoryId
     */
    @Override
    public List<KnowledgeResource> getResourcesByCategory(Long categoryId) {
        LambdaQueryWrapper<KnowledgeResource> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeResource::getStatus,1)
                .eq(KnowledgeResource::getCategoryId,categoryId)
                .orderByDesc(KnowledgeResource::getCreateTime);
        List<KnowledgeResource> knowledgeResources = knowledgeResourceMapper.selectList(wrapper);
        return  knowledgeResources;

    }

    /**
     * 模糊搜索资源
     *
     * @param keywords
     */
    @Override
    public List<KnowledgeResource> searchResources(String keywords) {

        // 如果关键词为空，直接返回空列表，避免查询全表
        if (keywords == null || keywords.trim().isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<KnowledgeResource> wrapper=new LambdaQueryWrapper<>();
        wrapper.like(keywords!=null&& !keywords.isEmpty(),KnowledgeResource::getResourceName,keywords)
                .eq(KnowledgeResource::getStatus,1)
                .orderByDesc(KnowledgeResource::getCreateTime)
                .last("LIMIT 100"); // 限制最多返回100条，保护数据库性能
        List<KnowledgeResource> knowledgeResources = knowledgeResourceMapper.selectList(wrapper);
        return  knowledgeResources;
    }

    /**
     * 获取下载次数统计
     */
    @Override
    public Map<String, Object> getDownloadStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 获取所有资源
        List<KnowledgeResource> list = knowledgeResourceMapper.selectList(null);

        // 2. 计算总资源数
        long totalResources = list.size();

        // 3. 计算总下载次数
        long totalDownloads = list.stream()
                .mapToLong(r -> r.getDownloadCount() == null ? 0 : r.getDownloadCount())
                .sum();

        // 4. 放入 Map
        stats.put("totalResources", totalResources);
        stats.put("totalDownloads", totalDownloads);

        return stats;
    }
    /**
     * 按分类统计资源数量
     */
    @Override
    public Map<Long, Long> getResourceCountByCategory() {
        Map<Long, Long> resourceCountMap = new HashMap<>();
        List<KnowledgeResource> list = knowledgeResourceMapper.selectList(null);

        for (KnowledgeResource resource : list) {
            Long categoryId = resource.getCategoryId();
            if (categoryId != null) {
                // merge 方法：如果 key 不存在，放入 1；如果存在，执行 (oldVal, newVal) -> oldVal + newVal
                resourceCountMap.merge(categoryId, 1L, Long::sum);
            }
        }
        return resourceCountMap;
    }

    /**
     * 获取下载日志列表（管理员）
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param resourceId
     */

    @Override
    public IPage<KnowledgeDownloadLog> getDownloadLogPage(int pageNum, int pageSize, Long userId, Long resourceId) {
        Page<KnowledgeDownloadLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeDownloadLog> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(userId != null, KnowledgeDownloadLog::getUserId, userId)
                .eq(resourceId != null, KnowledgeDownloadLog::getResourceId, resourceId)
                .orderByDesc(KnowledgeDownloadLog::getDownloadTime);

        knowledgeDownloadLogMapper.selectPage(page, queryWrapper);
        return page;
    }

    private Integer detectKnowledgeFileType(String extension) {
        if (extension == null) {
            log.warn("文件扩展名为空，默认归类为文档");
            return FileTypeEnum.DOCUMENT.getCode();
        }

        String ext = extension.toLowerCase();

        if (ext.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
            return FileTypeEnum.IMAGE.getCode();
        }
        else if (ext.matches("\\.(pdf|doc|docx|ppt|pptx)$")) {
            return FileTypeEnum.DOCUMENT.getCode();
        }
        else {
            log.warn("检测到未定义的文件类型: {}, 默认归类为文档"); // 修复日志占位符
            return FileTypeEnum.DOCUMENT.getCode();
        }
    }



}
