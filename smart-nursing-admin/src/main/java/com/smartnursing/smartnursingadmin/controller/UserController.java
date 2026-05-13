package com.smartnursing.smartnursingadmin.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartnursing.common.result.CommonResult;
import com.smartnursing.entity.SysRole;
import com.smartnursing.entity.SysUser;
import com.smartnursing.entity.SysUserRole;
import com.smartnursing.mapper.SysRoleMapper;
import com.smartnursing.mapper.SysUserMapper;
import com.smartnursing.service.SysUserRoleService;
import com.smartnursing.smartnursingadmin.security.LoginUserDetails;
import com.smartnursing.smartnursingadmin.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.smartnursing.common.enums.GlobalErrorCodeConstants.ADD_ERROR;
import static com.smartnursing.common.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.smartnursing.common.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static com.smartnursing.common.enums.GlobalErrorCodeConstants.LOGIN_ERROR;
import static com.smartnursing.common.enums.GlobalErrorCodeConstants.USERNAME_EXISTS;

/**
 * 用户相关 HTTP 接口：登录、注册等。
 */
@Tag(name = "用户管理", description = "用户登录、注册、权限管理等相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 开放注册时若请求里没带角色 ID 列表，则自动给该用户挂上这一条「默认角色」。
     * 数值必须与数据库 {@code sys_role} 表里真实存在的主键一致（例如普通用户角色 id=2）。
     */
    private static final long DEFAULT_REGISTER_ROLE_ID = 2L;

    /** Spring Security：用用户名密码做一次认证（内部会查库、验密）。 */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** 登录成功后签发 JWT。 */
    @Autowired
    private JwtUtil jwtUtil;

    /** MyBatis-Plus：对 {@code sys_user} 表的增删改查。 */
    @Autowired
    private SysUserMapper sysUserMapper;

    /** 与 Security 里配置的同一套加密器，注册时用来把明文密码编成 BCrypt 再入库。 */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 维护 {@code sys_user_role}：用户和角色之间的多对多关联。 */
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleMapper sysRoleMapper;


    @Operation(
            summary = "用户登录",
            description = "使用用户名和密码进行登录，成功后返回 JWT Token、用户信息和权限列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登录成功",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "402", description = "账号或密码错误"),
                    @ApiResponse(responseCode = "400", description = "请求参数不正确")
            }
    )

    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    );

            Authentication authentication = authenticationManager.authenticate(authToken);

            LoginUserDetails loginUser = (LoginUserDetails) authentication.getPrincipal();
            SysUser user = loginUser.getSysUser();
            Long userId = user.getId();

            String token = jwtUtil.generateToken(userId, user.getUsername());

            List<SysRole> roleEntities = sysRoleMapper.listRolesByUserId(userId);
            List<RoleBrief> roles = roleEntities.stream()
                    .map(r -> new RoleBrief(r.getId(), r.getRoleCode(), r.getRoleName()))
                    .toList();

            LoginResponse body = new LoginResponse(
                    token,
                    user.getUsername(),
                    userId,
                    List.copyOf(loginUser.getPermissionCodes()),
                    roles
            );
            return CommonResult.success(body);

        } catch (BadCredentialsException e) {
            return CommonResult.error(LOGIN_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.error(BAD_REQUEST);
        }

    }


    /** 登录成功返回体：含 token、账号、用户 id、权限标识、角色简要信息。 */
    @io.swagger.v3.oas.annotations.media.Schema(description = "登录响应数据")
    public record LoginResponse(
            @io.swagger.v3.oas.annotations.media.Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
            String token,
            @io.swagger.v3.oas.annotations.media.Schema(description = "用户名", example = "admin")
            String username,
            @io.swagger.v3.oas.annotations.media.Schema(description = "用户ID", example = "1")
            Long userId,
            @io.swagger.v3.oas.annotations.media.Schema(description = "权限列表", example = "[\"user:query\", \"user:edit\"]")
            List<String> permissions,
            @io.swagger.v3.oas.annotations.media.Schema(description = "角色列表")
            List<RoleBrief> roles
    ) {
    }

    @io.swagger.v3.oas.annotations.media.Schema(description = "角色简要信息")
    public record RoleBrief(
            @io.swagger.v3.oas.annotations.media.Schema(description = "角色ID", example = "1")
            Long roleId,
            @io.swagger.v3.oas.annotations.media.Schema(description = "角色编码", example = "ADMIN")
            String roleCode,
            @io.swagger.v3.oas.annotations.media.Schema(description = "角色名称", example = "管理员")
            String roleName
    ) {
    }

    /** 登录请求 JSON 对应字段。 */
    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "登录请求参数")
    public static class LoginRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "用户名", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED, example = "admin")
        private String username;
        @io.swagger.v3.oas.annotations.media.Schema(description = "密码", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED, example = "123456")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** 注册请求 JSON 对应字段。 */
    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "注册请求参数")
    public static class RegisterRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "登录账号", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED, example = "newuser")
        private String username;
        @io.swagger.v3.oas.annotations.media.Schema(description = "明文密码", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED, example = "123456")
        private String password;
        @io.swagger.v3.oas.annotations.media.Schema(description = "真实姓名", example = "张三")
        private String realName;
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号", example = "13800138000")
        private String phone;
        @io.swagger.v3.oas.annotations.media.Schema(description = "科室ID", example = "1")
        private Long deptId;
        @io.swagger.v3.oas.annotations.media.Schema(description = "角色ID列表", example = "[2, 3]")
        private List<Long> roleIds;
    }

    /**
     * 注册成功后的返回体：只带「新用户 id + 用户名」，不把密码等敏感信息返回去。
     * <p>
     * 是否成功请看外层 {@link CommonResult#getCode()}、失败原因看 {@link CommonResult#getMsg()}，
     * 不必在 data 里再套一层 success/message。
     */
    public record RegisterResponse(Long userId, String username) {
    }

    @Operation(
            summary = "用户注册",
            description = "新用户注册接口，自动分配默认角色，支持指定多个角色",
            responses = {
                    @ApiResponse(responseCode = "200", description = "注册成功",
                            content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
                    @ApiResponse(responseCode = "409", description = "用户名已存在"),
                    @ApiResponse(responseCode = "400", description = "请求参数不正确"),
                    @ApiResponse(responseCode = "500", description = "系统异常")
            }
    )
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        // 账号统一去掉首尾空格，避免 " abc " 与 "abc" 被当成两个账号
        String username = registerRequest.getUsername() != null ? registerRequest.getUsername().trim() : "";
        if (!StringUtils.hasText(username) || !StringUtils.hasText(registerRequest.getPassword())) {
            return CommonResult.error(BAD_REQUEST);
        }

        try {
            // 1. 查库：同名用户是否已存在
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, username);
            if (sysUserMapper.selectOne(wrapper) != null) {
                return CommonResult.error(USERNAME_EXISTS);
            }

            // 2. 解析本次要写入中间表的角色 id（去重、去 null；若为空则用默认角色）
            List<Long> roleIdsToBind = resolveRegisterRoleIds(registerRequest.getRoleIds());

            // 3. 组装实体并插入 sys_user（密码必须加密后再 set）
            SysUser newUser = new SysUser();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setRealName(registerRequest.getRealName());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setDeptId(registerRequest.getDeptId() != null ? registerRequest.getDeptId() : 1L);
            newUser.setStatus(1);
            newUser.setCreateTime(new Date());

            if (sysUserMapper.insert(newUser) <= 0) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return CommonResult.error(ADD_ERROR);
            }

            // 4. 插入后 newUser.getId() 由 MyBatis-Plus 回填，用于写 sys_user_role.user_id
            List<SysUserRole> userRoles = new ArrayList<>(roleIdsToBind.size());
            for (Long roleId : roleIdsToBind) {
                SysUserRole link = new SysUserRole();
                link.setUserId(newUser.getId());
                link.setRoleId(roleId);
                userRoles.add(link);
            }
            if (!sysUserRoleService.saveBatch(userRoles)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return CommonResult.error(ADD_ERROR);
            }

            return CommonResult.success(new RegisterResponse(newUser.getId(), newUser.getUsername()));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 把前端传来的角色 id 列表整理成「最终要写入 sys_user_role」的列表：
     * null/空/全是 null → 只绑默认角色；否则去掉 null、去重，顺序保留 stream 出现顺序。
     */
    private static List<Long> resolveRegisterRoleIds(List<Long> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of(DEFAULT_REGISTER_ROLE_ID);
        }
        List<Long> distinct = requested.stream().filter(Objects::nonNull).distinct().toList();
        if (distinct.isEmpty()) {
            return List.of(DEFAULT_REGISTER_ROLE_ID);
        }
        return distinct;
    }
}
