package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.mall.domain.dto.AdminLoginBody;
import com.qingguoji.mall.mall.service.IAdminService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端登录（M-06，公开接口）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api")
@RequiredArgsConstructor
public class AdminLoginController extends BaseController {

    private final IAdminService adminService;

    /** M-06 管理员登录（独立于买家体系） */
    @PostMapping("/login")
    public AjaxResult<Map<String, String>> login(@RequestBody AdminLoginBody body) {
        String token = adminService.login(body.getUsername(), body.getPassword());
        if (token == null) {
            return AjaxResult.error("用户名或密码错误");
        }
        return success(Map.of("token", token));
    }
}
