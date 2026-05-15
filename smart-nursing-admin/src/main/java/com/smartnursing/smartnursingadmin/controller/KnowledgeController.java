package com.smartnursing.smartnursingadmin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartnursing.common.result.CommonResult;
import com.smartnursing.entity.KnowledgeCategory;
import com.smartnursing.entity.KnowledgeDownloadLog;
import com.smartnursing.entity.KnowledgeResource;
import com.smartnursing.knowledge.service.KnowledgeCategoryService;
import com.smartnursing.knowledge.service.KnowledgeService;
import com.smartnursing.smartnursingadmin.security.LoginUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.smartnursing.common.enums.GlobalErrorCodeConstants.ADD_ERROR;
import static com.smartnursing.common.enums.GlobalErrorCodeConstants.DELETE_ERROR;

@Tag(name="知识资源管理")
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {
    @Resource
    private KnowledgeService knowledgeService;
    @Resource
    private KnowledgeCategoryService knowledgeCategoryService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUserDetails) {
            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            return userDetails.getSysUser().getId();
        }
        throw new RuntimeException("未登录或登录已过期");
    }



    // ==================== 分类管理接口 ====================

    //分类管理接口 (Category)
    @Operation(summary = "获取所有分类列表", description = "返回系统中所有的知识分类，用于下拉框选择等场景")
    @GetMapping("/category/list")
    @PreAuthorize("hasAnyAuthority('knowledge:category:query')")
    public CommonResult<List<KnowledgeCategory>> getCategoryList(){
        List<KnowledgeCategory> allCategories = knowledgeCategoryService.getAllCategories();
        return  CommonResult.success(allCategories);
    }

    @Operation(summary = "分类分页查询", description = "支持分页获取知识分类列表")
    @PreAuthorize("hasAuthority('knowledge:category:query')")
    @GetMapping("/category/page")
    public CommonResult<List<KnowledgeCategory>> getCategoryPage(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {

        // 调用 Service 层的方法，传入页码和每页数量
        IPage<KnowledgeCategory> pageResult = knowledgeCategoryService.getCategoryPage(pageNum, pageSize);

        // 使用 successPageData 方法，它会自动处理 total 和 records
        return CommonResult.successPageData(pageResult);
    }


    @Operation(summary = "新增分类", description = "创建一个新的知识资源分类")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @PostMapping("/category/add")
    public CommonResult<Boolean> addNewCategory(@RequestBody KnowledgeCategory knowledgeCategory){
        boolean isSaved = knowledgeCategoryService.saveCategory(knowledgeCategory);
        if(isSaved){
            return CommonResult.success();
        }
        return CommonResult.error(ADD_ERROR);

    }

    @Operation(summary = "删除分类", description = "根据id删除知识资源分类,如果分类下还存在资源会报错")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @DeleteMapping("/category/delete/{id}")
    public CommonResult<Boolean> deleteCategoryById(@PathVariable("id") Long categoryId){
        boolean isDeleted = knowledgeCategoryService.deleteCategory(categoryId);
        if(isDeleted){
            return CommonResult.success();
        }
        return CommonResult.error(DELETE_ERROR);

    }


    @Operation(summary = "编辑分类", description = "修改分类名称或排序")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @PostMapping("/category/update")
    public CommonResult<Boolean> updateCategory(@RequestBody KnowledgeCategory category) {
        return CommonResult.success(knowledgeCategoryService.updateCategory(category));
    }


    // ==================== 资源管理接口 ====================

    @Operation(summary = "上传知识资源", description = "支持 PDF, Word, PPT, 图片等格式")
    @PreAuthorize("hasAuthority('knowledge:resource:edit') and hasAuthority('file:upload')")
    @PostMapping("/upload")
    public CommonResult<String> uploadKnowledgeResource(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "分类ID", required = true)
            @RequestParam Long categoryId) throws Exception { // 直接抛出异常交给全局处理

        Long currentUserId = getCurrentUserId();
        // Service 层如果出错会直接抛异常，这里只需要关心成功的情况
        String resourceId = knowledgeService.uploadResource(file, currentUserId, categoryId);

        return CommonResult.success(resourceId);
    }

    @Operation(summary = "编辑资源信息", description = "修改资源标题、分类等元数据（不修改文件）")
    @PreAuthorize("hasAuthority('knowledge:resource:edit')")
    @PostMapping("/update")
    public CommonResult<Boolean> updateResource(@RequestBody KnowledgeResource resource) {
        return CommonResult.success(knowledgeService.updateResource(resource));
    }

    @Operation(summary = "删除资源", description = "删除资源及其关联的文件和日志")
    @PreAuthorize("hasAuthority('knowledge:resource:edit')")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> deleteResource(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        knowledgeService.deleteResource(id, userId);
        return CommonResult.success(true);
    }

    @Operation(summary = "获取资源详情", description = "根据ID获取资源详细信息")
    @PreAuthorize("hasAuthority('knowledge:resource:query')")
    @GetMapping("/detail/{id}")
    public CommonResult<KnowledgeResource> detail(@PathVariable Long id) {
        return CommonResult.success(knowledgeService.getResourceDetail(id));
    }


    @Operation(summary = "生成下载链接", description = "输入资源ID，生成5分钟有效的预签名URL并记录日志")
    @PreAuthorize("hasAuthority('knowledge:resource:query')")
    @GetMapping("/download/{id}")
    public CommonResult<String> getDownloadURL(@PathVariable("id") Long resourceId) throws Exception {
        Long currentUserId = getCurrentUserId();
        String downloadUrl = knowledgeService.getDownloadUrl(resourceId, currentUserId);
        return CommonResult.success(downloadUrl);
    }

    @Operation(summary = "资源列表分页查询", description = "支持按分类、名称、状态筛选")
    @PreAuthorize("hasAuthority('knowledge:resource:query')")
    @GetMapping("/page")
    public CommonResult<List<KnowledgeResource>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) Integer status) {
        IPage<KnowledgeResource> pageResult = knowledgeService.getResourcePage(pageNum, pageSize, categoryId, resourceName, status);
        return CommonResult.successPageData(pageResult);
    }

    @Operation(summary = "根据分类查询资源", description = "获取指定分类下的所有上架资源")
    @PreAuthorize("hasAuthority('knowledge:resource:query')")
    @GetMapping("/category/{categoryId}")
    public CommonResult<List<KnowledgeResource>> getByCategory(@PathVariable Long categoryId) {
        return CommonResult.success(knowledgeService.getResourcesByCategory(categoryId));
    }


    @Operation(summary = "模糊搜索资源", description = "根据关键词搜索资源名称")
    @PreAuthorize("hasAuthority('knowledge:resource:query')")
    @GetMapping("/search")
    public CommonResult<List<KnowledgeResource>> search(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keywords) {
        return CommonResult.success(knowledgeService.searchResources(keywords));
    }

    @Operation(summary = "更新资源状态", description = "上架/下架资源")
    @PreAuthorize("hasAuthority('knowledge:resource:edit')")
    @PostMapping("/status")
    public CommonResult<Boolean> updateStatus(
            @RequestParam Long resourceId,
            @RequestParam Integer status) {
        KnowledgeResource resource = new KnowledgeResource();
        resource.setId(resourceId);
        resource.setStatus(status);
        return CommonResult.success(knowledgeService.updateResource(resource));
    }

    // ==================== 日志与统计接口 ====================

    @Operation(summary = "下载日志列表", description = "管理员查看下载记录")
    @PreAuthorize("hasAuthority('knowledge:log:query')")
    @GetMapping("/log/page")
    public CommonResult<List<KnowledgeDownloadLog>> logPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId) {
        IPage<KnowledgeDownloadLog> pageResult = knowledgeService.getDownloadLogPage(pageNum, pageSize, userId, resourceId);
        return CommonResult.successPageData(pageResult);
    }

    @Operation(summary = "下载统计", description = "获取总下载次数和资源数量统计")
    @PreAuthorize("hasAuthority('knowledge:stat:query')")
    @GetMapping("/stat/download")
    public CommonResult<Map<String, Object>> downloadStatistics() {
        return CommonResult.success(knowledgeService.getDownloadStatistics());
    }

    @Operation(summary = "按分类统计资源", description = "统计每个分类下的资源数量")
    @PreAuthorize("hasAuthority('knowledge:stat:query')")
    @GetMapping("/stat/category")
    public CommonResult<Map<Long, Long>> resourceCountByCategory() {
        return CommonResult.success(knowledgeService.getResourceCountByCategory());
    }




}
