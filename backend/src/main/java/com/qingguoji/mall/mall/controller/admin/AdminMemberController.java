package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.common.core.page.TableDataInfo;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.PointsLog;
import com.qingguoji.mall.mall.domain.dto.AdjustBody;
import com.qingguoji.mall.mall.service.IMemberService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端会员管理（M-07）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api/members")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminMemberController extends BaseController {

    private final IMemberService memberService;

    /** M-07 会员列表（按成长值倒序） */
    @GetMapping
    public AjaxResult<TableDataInfo<Member>> list(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return getDataTable(
                memberService.selectMemberPage(
                        buildPageRequest(page, size, "growth", Sort.Direction.DESC)));
    }

    /** M-07 积分人工调整（留痕） */
    @PostMapping("/{id}/points")
    public AjaxResult<Map<String, Object>> adjustPoints(
            @PathVariable Long id, @RequestBody AdjustBody body) {
        return success(
                Map.of(
                        "points",
                        memberService.adjustPoints(id, body.getDelta(), body.getReason())));
    }

    /** M-07 会员积分流水 */
    @GetMapping("/{id}/points/logs")
    public AjaxResult<TableDataInfo<PointsLog>> pointsLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return getDataTable(
                memberService.selectPointsLogPage(
                        id, buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }
}
