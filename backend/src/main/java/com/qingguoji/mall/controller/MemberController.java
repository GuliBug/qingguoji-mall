package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.BizException;
import com.qingguoji.mall.common.R;
import com.qingguoji.mall.common.TokenStore;
import com.qingguoji.mall.domain.Member;
import com.qingguoji.mall.domain.PointsLog;
import com.qingguoji.mall.repo.MemberRepo;
import com.qingguoji.mall.service.MallService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** U-01~U-03 + V-04/V-05/V-07 会员接口 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepo memberRepo;
    private final TokenStore tokenStore;
    private final MallService mallService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginReq req) {
        Member m = mallService.login(req.getPhone(), req.getSmsCode());
        String token = tokenStore.createToken(m.getId());
        return R.ok(Map.of("token", token, "member", m, "levelName", m.levelName()));
    }

    @GetMapping("/me")
    public R<?> me(@RequestHeader(value = "X-Token", required = false) String token) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        Member m = memberRepo.findById(mid).orElseThrow(() -> new BizException("会员不存在"));
        return R.ok(Map.of(
                "member", m,
                "level", m.level(),
                "levelName", m.levelName(),
                "pointRate", m.pointRate(),
                "discount", m.discount()));
    }

    /** V-07 积分明细 */
    @GetMapping("/points/logs")
    public R<Page<PointsLog>> pointsLogs(@RequestHeader("X-Token") String token,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        return R.ok(mallService.pointsLogs(mid, page, size));
    }

    /** V-05 每日签到 */
    @PostMapping("/checkin")
    public R<?> checkin(@RequestHeader("X-Token") String token) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        int points = mallService.checkin(mid);
        return R.ok(Map.of("points", points));
    }

    /** V-04 扫码积分（AC-10 防重/防超上限） */
    @PostMapping("/scan")
    public R<?> scan(@RequestHeader("X-Token") String token, @RequestBody ScanReq req) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        int points = mallService.scanCode(mid, req.getCode());
        return R.ok(Map.of("points", points));
    }

    @Data
    public static class LoginReq {
        private String phone;
        private String smsCode;
    }

    @Data
    public static class ScanReq {
        private String code;
    }
}
