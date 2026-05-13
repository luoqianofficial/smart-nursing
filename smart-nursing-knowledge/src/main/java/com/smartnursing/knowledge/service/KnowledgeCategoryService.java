package com.smartnursing.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnursing.entity.KnowledgeCategory;

import java.util.List;

public interface KnowledgeCategoryService extends IService<KnowledgeCategory> {
    /**
     * 新增分类
     */
    boolean saveCategory(KnowledgeCategory category);

    /**
     * 编辑分类
     */
    boolean updateCategory(KnowledgeCategory category);

    /**
     * 删除分类（有资源时不允许删除）
     */
    boolean deleteCategory(Long categoryId);

    /**
     * 分类列表查询（分页）
     */
    IPage<KnowledgeCategory> getCategoryPage(int pageNum, int pageSize);

    /**
     * 获取所有分类列表（不分页）
     */
    List<KnowledgeCategory> getAllCategories();
}
