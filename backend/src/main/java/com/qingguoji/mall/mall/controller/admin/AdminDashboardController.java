package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.service.IAdminService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端数据看板（M-01）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminDashboardController extends BaseController {

    private final IAdminService adminService;

    /** M-01 数据看板指标 */
    @GetMapping("/dashboard")
    public AjaxResult<Map<String, Object>> dashboard() {
        return success(adminService.selectDashboard());
    }
}
