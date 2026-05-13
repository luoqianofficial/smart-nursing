package com.smartnursing.smartnursingadmin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.smartnursing.smartnursingadmin.utils.JwtUtil;
import com.smartnursing.smartnursingadmin.service.impl.SysUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 每次请求都会经过这里，验证 token 是否有效
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("JWT Filter 拦截请求: {}", requestURI);

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.getUsernameFromToken(token);
                log.debug("解析出的用户名: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("UserDetails 加载成功: {}, 用户权限: {}", userDetails.getUsername(), userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("认证成功: {}", username);
                } else {
                    log.debug("用户名解析失败或 SecurityContext 已有认证");
                }

            } catch (Exception e) {
                log.error("JWT token 验证失败: {}", e.getMessage());
            }
        } else {
            log.debug("没有 Authorization Header 或格式不正确");
        }

        filterChain.doFilter(request, response);
    }
}


