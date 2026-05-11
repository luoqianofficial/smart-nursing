package com.smartnursing.smartnursingadmin.controller;

import com.smartnursing.common.result.CommonResult;
import com.smartnursing.entity.DigitalResource;
import com.smartnursing.service.DigitalService;
import com.smartnursing.smartnursingadmin.security.LoginUserDetails;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/digital")
public class DigitalController {

    @Resource
    private DigitalService digitalService;

    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/list")
    public CommonResult<List<DigitalResource>> list() {
        List<DigitalResource> list = digitalService.list();
        return CommonResult.success(list);
    }

    @PreAuthorize("hasAuthority('digital:resource:edit') and hasAuthority('file:upload')")
    @PostMapping("/upload")
    public CommonResult<String> upload(MultipartFile file) throws Exception {
        Long userId=getCurrentUserId();
        String resourceId = digitalService.uploadMedia(file, userId);
        return CommonResult.success(resourceId);
    }

    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/play/{id}")
    public CommonResult<String> play(@PathVariable Long id) {
        Long userId=getCurrentUserId();
        digitalService.play(id, userId);
        return CommonResult.success("播放成功");
    }

    @PreAuthorize("hasAuthority('digital:resource:query')")
    @GetMapping("/share/{id}")
    public CommonResult<String> share(@PathVariable Long id) {
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
}
