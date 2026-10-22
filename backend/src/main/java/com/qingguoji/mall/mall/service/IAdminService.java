package com.qingguoji.mall.mall.service;

import java.util.Map;

/**
 * 管理端通用服务（M-01 数据看板 / M-06 管理员登录）
 *
 * @author qingguoji
 */
public interface IAdminService {

    /** M-06 管理员登录，成功返回令牌，失败返回 null */
    String login(String username, String password);

    /** M-01 数据看板指标 */
    Map<String, Object> selectDashboard();
}
