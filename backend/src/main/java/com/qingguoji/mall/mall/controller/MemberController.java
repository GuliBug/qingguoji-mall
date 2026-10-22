package com.qingguoji.mall.mall.controller;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.framework.web.service.TokenService;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.PointsLog;
import com.qingguoji.mall.mall.domain.dto.MemberLoginBody;
import com.qingguoji.mall.mall.domain.dto.ScanBody;
import com.qingguoji.mall.mall.service.IMemberService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员接口（U-01~U-03 / V-04~V-07）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController extends BaseController {

    private final IMemberService memberService;
    private final TokenService tokenService;

    /** U-01 手机号验证码登录（公开接口） */
    @PostMapping("/login")
    public AjaxResult<Map<String, Object>> login(@RequestBody MemberLoginBody body) {
        Member member = memberService.loginByPhone(body.getPhone(), body.getSmsCode());
        String token = tokenService.createToken(member.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("member", member);
        result.put("levelName", member.levelName());
        return success(result);
    }

    /** U-03 当前会员信息 */
    @GetMapping("/me")
    @RequireLogin(LoginType.MEMBER)
    public AjaxResult<Map<String, Object>> me() {
        Member member = memberService.selectMemberById(getMemberId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("member", member);
        result.put("level", member.level());
        result.put("levelName", member.levelName());
        result.put("pointRate", member.pointRate());
        result.put("discount", member.discount());
        return success(result);
    }

    /** V-07 积分明细（H5 端按 Spring Page 的 content 字段消费，勿改 TableDataInfo） */
    @GetMapping("/points/logs")
    @RequireLogin(LoginType.MEMBER)
    public AjaxResult<Page<PointsLog>> pointsLogs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return success(
                memberService.selectPointsLogPage(
                        getMemberId(),
                        buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }

    /** V-05 每日签到 */
    @PostMapping("/checkin")
    @RequireLogin(LoginType.MEMBER)
    public AjaxResult<Map<String, Object>> checkin() {
        return success(Map.of("points", memberService.checkin(getMemberId())));
    }

    /** V-04 扫码积分（AC-10 防重 / 防超上限） */
    @PostMapping("/scan")
    @RequireLogin(LoginType.MEMBER)
    public AjaxResult<Map<String, Object>> scan(@RequestBody ScanBody body) {
        return success(Map.of("points", memberService.scanCode(getMemberId(), body.getCode())));
    }
}
