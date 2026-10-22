package com.qingguoji.mall.framework.web.service;

import com.qingguoji.mall.common.constant.Constants;
import com.qingguoji.mall.common.utils.StringUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 令牌服务（参考若依 TokenService）
 *
 * <p>演示环境使用内存表保存 token → 主体映射，进程重启即失效。 正式环境按 spec U-02 替换为 JWT（HS512 + 7 天过期），方法签名不变。
 *
 * @author qingguoji
 */
@Slf4j
@Service
public class TokenService {

    /** 买家：token → memberId */
    private final Map<String, Long> memberTokens = new ConcurrentHashMap<>();

    /** 卖家：token → 管理员账号 */
    private final Map<String, String> adminTokens = new ConcurrentHashMap<>();

    /** 买家登录后签发令牌 */
    public String createToken(Long memberId) {
        String token =
                Constants.MEMBER_TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "");
        memberTokens.put(token, memberId);
        return token;
    }

    /** 由令牌取买家 ID，无效返回 null */
    public Long getMemberId(String token) {
        return token == null ? null : memberTokens.get(token);
    }

    /** 管理员登录，成功返回令牌，失败返回 null */
    public String adminLogin(String username, String password) {
        if (!Constants.ADMIN_USERNAME.equals(username)
                || !Constants.ADMIN_PASSWORD.equals(password)) {
            return null;
        }
        String token = Constants.ADMIN_TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "");
        adminTokens.put(token, username);
        log.info("管理员 {} 登录成功", username);
        return token;
    }

    /** 校验管理端令牌，返回管理员账号；无效返回 null */
    public String getAdminName(String token) {
        return token == null ? null : adminTokens.get(token);
    }

    /** 是否管理端有效令牌 */
    public boolean isAdmin(String token) {
        return StringUtils.isNotEmpty(getAdminName(token));
    }
}
