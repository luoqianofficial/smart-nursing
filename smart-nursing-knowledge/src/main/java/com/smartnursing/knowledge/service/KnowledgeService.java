package com.smartnursing.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnursing.entity.KnowledgeDownloadLog;
import com.smartnursing.entity.KnowledgeResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface KnowledgeService  extends IService<KnowledgeResource> {
    /**
     * 上传知识资源
     * @param file 上传的文件
     * @param userId 上传人ID
     * @param categoryId 分类ID
     * @return 资源ID
     */
    String uploadResource(MultipartFile file, Long userId, Long categoryId) throws Exception;

    /**
     * 编辑知识资源信息（不修改文件）
     */
    boolean updateResource(KnowledgeResource resource);

    /**
     * 删除知识资源（同时删除MinIO文件 + file_info记录 + 下载日志）
     */
    boolean deleteResource(Long resourceId, Long userId);

    /**
     * 资源列表分页查询（支持按分类、名称筛选）
     */
    IPage<KnowledgeResource> getResourcePage(int pageNum, int pageSize, Long categoryId, String resourceName, Integer status);

    /**
     * 获取资源详情
     */
    KnowledgeResource getResourceDetail(Long resourceId);

    /**
     * 生成下载地址（返回MinIO预签名URL）
     * 每次调用会：download_count + 1，插入下载日志
     */
    String getDownloadUrl(Long resourceId, Long userId) throws Exception;

    /**
     * 根据分类ID查询资源列表
     */
    List<KnowledgeResource> getResourcesByCategory(Long categoryId);

    /**
     * 模糊搜索资源
     */
    List<KnowledgeResource> searchResources(String keywords);

    /**
     * 获取下载次数统计
     */
    Map<String, Object> getDownloadStatistics();

    /**
     * 按分类统计资源数量
     */
    Map<Long, Long> getResourceCountByCategory();

    /**
     * 获取下载日志列表（管理员）
     */
    IPage<KnowledgeDownloadLog> getDownloadLogPage(int pageNum, int pageSize, Long userId, Long resourceId);
}
