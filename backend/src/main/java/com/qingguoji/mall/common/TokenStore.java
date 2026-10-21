package com.qingguoji.mall.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简登录态：token -> memberId（演示用内存版）。
 * 正式版按 spec U-02 换成 JWT（HS512 + 7天过期），接口签名不变。
 */
@Component
public class TokenStore {

    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private final Map<String, String> adminTokens = new ConcurrentHashMap<>();

    public String createToken(Long memberId) {
        String token = "MBR-" + UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, memberId);
        return token;
    }

    public Long memberId(String token) {
        if (token == null) return null;
        return tokens.get(token);
    }

    public String adminLogin(String username, String password) {
        // M-06 管理端独立账号体系（演示：admin/admin123）
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = "ADM-" + UUID.randomUUID().toString().replace("-", "");
            adminTokens.put(token, username);
            return token;
        }
        return null;
    }

    public boolean isAdmin(String token) {
        return token != null && adminTokens.containsKey(token);
    }
}
