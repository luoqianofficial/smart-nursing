package com.smartnursing.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnursing.entity.KnowledgeCategory;
import com.smartnursing.entity.KnowledgeResource;
import com.smartnursing.knowledge.mapper.KnowledgeCategoryMapper;
import com.smartnursing.knowledge.mapper.KnowledgeResourceMapper;
import com.smartnursing.knowledge.service.KnowledgeCategoryService;
import com.smartnursing.knowledge.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KnowledgeCategoryServiceImpl extends ServiceImpl<KnowledgeCategoryMapper, KnowledgeCategory> implements KnowledgeCategoryService {
    @Autowired
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    @Autowired
    private KnowledgeResourceMapper knowledgeResourceMapper;

    /**
     * 新增分类
     *
     * @param category
     */
    @Override
    public boolean saveCategory(KnowledgeCategory category) {
        // 建议：如果前端没传 sort，给个默认值
        if (category.getSort() == null) {
            category.setSort(0);
        }
        return knowledgeCategoryMapper.insert(category) > 0;
    }


    /**
     * 编辑分类
     *
     * @param category
     */
    @Override
    public boolean updateCategory(KnowledgeCategory category) {
        // 先检查分类是否存在
        KnowledgeCategory original = knowledgeCategoryMapper.selectById(category.getId());
        if (original == null) {
            return false;
        }
        return knowledgeCategoryMapper.updateById(category) > 0;
    }

    /**
     * 删除分类（有资源时不允许删除）
     *
     * @param categoryId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long categoryId) {
        // 1. 检查该分类下是否有资源
        LambdaQueryWrapper<KnowledgeResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeResource::getCategoryId, categoryId);

        long count = knowledgeResourceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该分类下存在 " + count + " 个资源，无法删除");
        }

        // 2. 执行删除
        return knowledgeCategoryMapper.deleteById(categoryId) > 0;
    }

    /**
     * 分类列表查询（分页）
     *
     * @param pageNum
     * @param pageSize
     */
    @Override
    public IPage<KnowledgeCategory> getCategoryPage(int pageNum, int pageSize) {
        Page<KnowledgeCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();

        // 按 sort 升序排列，sort 相同的按 id 降序
        wrapper.orderByAsc(KnowledgeCategory::getSort)
                .orderByDesc(KnowledgeCategory::getId);

        knowledgeCategoryMapper.selectPage(page, wrapper);
        return page;
    }

    /**
     * 获取所有分类列表（不分页）
     */
    @Override
    public List<KnowledgeCategory> getAllCategories() {
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(KnowledgeCategory::getSort);
        return knowledgeCategoryMapper.selectList(wrapper);
    }
}
