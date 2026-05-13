package com.smartnursing.smartnursingadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartnursing.common.result.CommonResult;
import com.smartnursing.entity.DigitalPlayLog;
import com.smartnursing.entity.DigitalResource;
import com.smartnursing.service.DigitalService;
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

@Tag(name = "数字资源管理", description = "数字多媒体资源的上传、播放、分享等接口")
@RestController
@RequestMapping("/digital")
public class DigitalController {

    @Resource
    private DigitalService digitalService;

    @Operation(summary = "获取数字资源列表", description = "查询所有数字资源列表")
    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/list")
    public CommonResult<List<DigitalResource>> list() {
        List<DigitalResource> list = digitalService.list();
        return CommonResult.success(list);
    }

    @Operation(summary = "上传数字资源", description = "上传视频、音频、图片等多媒体文件")
    @PreAuthorize("hasAuthority('digital:resource:edit') and hasAuthority('file:upload')")
    @PostMapping("/upload")
    public CommonResult<String> upload(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file) throws Exception {
        Long userId=getCurrentUserId();
        String resourceId = digitalService.uploadMedia(file, userId);
        return CommonResult.success(resourceId);
    }

    @Operation(summary = "记录播放", description = "记录用户播放行为，增加播放次数")
    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/play/{id}")
    public CommonResult<String> play(
            @Parameter(description = "资源ID", required = true, example = "1")
            @PathVariable Long id) {
        Long userId=getCurrentUserId();
        digitalService.play(id, userId);
        return CommonResult.success("播放成功");
    }

    @Operation(summary = "生成分享链接", description = "生成资源的临时分享链接（5分钟有效），同时记录播放")
    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/share/{id}")
    public CommonResult<String> share(
            @Parameter(description = "资源ID", required = true, example = "1")
            @PathVariable Long id) {
        Long userId = getCurrentUserId();
        String url = digitalService.createShareUrl(id, userId);
        return CommonResult.success(url);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUserDetails) {
            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            return userDetails.getSysUser().getId();
        }
        throw new RuntimeException("未登录或登录已过期");
    }

    @Operation(summary = "获取资源详情", description = "根据ID获取资源详细信息")
    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/detail/{id}")
    public CommonResult<DigitalResource> detail(
            @Parameter(description = "资源ID", required = true, example = "1")
            @PathVariable Long id) {
        return CommonResult.success(digitalService.getResourceDetail(id));
    }

    @Operation(summary = "删除资源", description = "删除资源及其关联的文件和日志")
    @PreAuthorize("hasAuthority('digital:resource:edit')")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> delete(
            @Parameter(description = "资源ID", required = true, example = "1")
            @PathVariable Long id) {
        Long userId = getCurrentUserId();
        return CommonResult.success(digitalService.deleteResource(id, userId));
    }




    @Operation(summary = "编辑资源信息", description = "修改资源标题、分类等信息")
    @PreAuthorize("hasAuthority('digital:resource:edit')")
    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody DigitalResource resource) {
        return CommonResult.success(digitalService.updateResource(resource));
    }


    @Operation(summary = "资源列表分页查询", description = "支持按名称、类型、状态筛选")
    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/page")
    public CommonResult<List<DigitalResource>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) Integer mediaType,
            @RequestParam(required = false) Integer status) {
        Page<DigitalResource> pageResult = digitalService.getResourcePage(pageNum, pageSize, resourceName, mediaType, status);
        return CommonResult.successPageData(pageResult);
    }

    @Operation(summary = "更新资源状态", description = "上架/下架资源")
    @PreAuthorize("hasAuthority('digital:resource:edit')")
    @PostMapping("/status")
    public CommonResult<Boolean> updateStatus(
            @RequestParam Long resourceId,
            @RequestParam Integer status) {
        return CommonResult.success(digitalService.updateStatus(resourceId, status));
    }

    @Operation(summary = "播放日志列表", description = "管理员查看播放日志")
    @PreAuthorize("hasAuthority('digital:log:query')")
    @GetMapping("/log/page")
    public CommonResult<List<DigitalPlayLog>> logPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId) {
        Page<DigitalPlayLog> pageResult = digitalService.getPlayLogPage(pageNum, pageSize, userId, resourceId);
        return CommonResult.successPageData(pageResult);
    }

    @Operation(summary = "按媒体类型统计播放量", description = "统计视频、音频、图片的播放次数")
    @PreAuthorize("hasAuthority('digital:stat:query')")
    @GetMapping("/stat/mediatype")
    public CommonResult<Map<Integer, Long>> statByMediaType() {
        return CommonResult.success(digitalService.getPlayCountByMediaType());
    }



}
