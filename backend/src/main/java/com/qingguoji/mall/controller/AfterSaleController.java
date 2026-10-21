package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.PageResult;
import com.qingguoji.mall.common.R;
import com.qingguoji.mall.common.TokenStore;
import com.qingguoji.mall.domain.AfterSale;
import com.qingguoji.mall.repo.AfterSaleRepo;
import com.qingguoji.mall.service.MallService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** A-01~A-09 客户端售后接口 */
@RestController
@RequestMapping("/api/aftersale")
@RequiredArgsConstructor
public class AfterSaleController {

    private final TokenStore tokenStore;
    private final MallService mallService;
    private final AfterSaleRepo afterSaleRepo;

    /** A-02/A-03 申请售后 */
    @PostMapping
    public R<AfterSale> apply(@RequestHeader("X-Token") String token, @RequestBody ApplyReq req) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        return R.ok(mallService.applyAfterSale(mid, req.getOrderItemId(), req.getType(),
                req.getQty(), req.getReason(), req.getDescription()));
    }

    @GetMapping("/my")
    public R<Page<AfterSale>> my(@RequestHeader("X-Token") String token,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        return R.ok(afterSaleRepo.findByMemberId(mid, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    /** A-07 买家填写退货物流单号 */
    @PostMapping("/{id}/return-logistics")
    public R<Void> fillLogistics(@RequestHeader("X-Token") String token, @PathVariable Long id, @RequestBody Map<String, String> body) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        mallService.fillReturnLogistics(id, mid, body.get("logisticsNo"));
        return R.ok();
    }

    @Data
    public static class ApplyReq {
        private Long orderItemId;
        /** REFUND_ONLY / RETURN_REFUND */
        private String type;
        private Integer qty;
        private String reason;
        private String description;
    }
}
