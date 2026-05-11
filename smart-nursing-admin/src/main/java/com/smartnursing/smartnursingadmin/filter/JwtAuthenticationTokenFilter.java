package com.smartnursing.smartnursingadmin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // 添加日志：请求路径
        System.out.println("=== JWT Filter 拦截请求 ===");
        System.out.println("请求URL: " + request.getRequestURI());

        // 1️⃣ 从请求头中获取 token
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        // 2️⃣ 检查 header 是否存在且以 Bearer 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token(前20字符): " + token.substring(0, Math.min(20, token.length())) + "...");

            try {
                // 先获取用户名（推荐从这里开始）
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("解析出的用户名: " + username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("UserDetails 加载成功: " + userDetails.getUsername());
                    System.out.println("用户密码(密文): " + userDetails.getPassword());
                    System.out.println("用户权限: " + userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("✅ 认证信息已设置到 SecurityContext");
                } else {
                    System.out.println("用户名解析失败或 SecurityContext 已有认证");
                }

            } catch (Exception e) {
                System.err.println("❌ JWT token 验证失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("没有 Authorization Header 或格式不正确");
        }

        // 9️⃣ 继续执行后续的过滤器
        filterChain.doFilter(request, response);
    }
}


