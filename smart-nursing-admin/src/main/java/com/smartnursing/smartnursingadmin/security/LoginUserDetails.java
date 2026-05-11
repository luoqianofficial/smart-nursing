package com.smartnursing.smartnursingadmin.security;

import com.smartnursing.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoginUserDetails implements UserDetails {

    private final SysUser sysUser;              // 持有用户实体
    private final List<String> permissions;     // 持有权限列表

    // 构造方法：从外部传入需要的数据
    public LoginUserDetails(SysUser sysUser, List<String> permissions) {
        this.sysUser = sysUser;
        this.permissions = permissions != null ? permissions : Collections.emptyList();
    }

    /** 权限标识列表，供登录响应与业务使用（与 {@link #getAuthorities()} 一致）。 */
    public List<String> getPermissionCodes() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();   // 从传入的用户对象中获取
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;    // 业务上通常返回 true，如需精确控制可从 sysUser 中读取字段
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sysUser.getStatus() == 1;
    }

    // 可选：提供获取原始用户实体的方法
    public SysUser getSysUser() {
        return sysUser;
    }
}
