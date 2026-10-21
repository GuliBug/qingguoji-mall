package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.*;
import com.qingguoji.mall.domain.*;
import com.qingguoji.mall.repo.*;
import com.qingguoji.mall.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端接口（M-01~M-09，参考若依后台风格：/admin-api 前缀 + 独立登录态）
 * 登录：admin / admin123
 */
@RestController
@RequestMapping("/admin-api")
@RequiredArgsConstructor
public class AdminController {

    private final TokenStore tokenStore;
    private final ProductRepo productRepo;
    private final ProductSkuRepo skuRepo;
    private final OrderRepo orderRepo;
    private final AfterSaleRepo afterSaleRepo;
    private final MemberRepo memberRepo;
    private final PointsLogRepo pointsLogRepo;
    private final MallService mallService;

    /** M-06 管理员登录（独立于买家体系） */
    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = tokenStore.adminLogin(body.get("username"), body.get("password"));
        if (token == null) return R.fail("用户名或密码错误");
        return R.ok(Map.of("token", token));
    }

    private R<Void> check(String token) {
        return tokenStore.isAdmin(token) ? R.ok() : R.unauthorized("管理员未登录");
    }

    /** M-01 数据看板 */
    @GetMapping("/dashboard")
    public R<?> dashboard(@RequestHeader("X-Token") String token) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        long waitShip = orderRepo.findByStatus("PAID", PageRequest.of(0, 1)).getTotalElements();
        long waitAudit = afterSaleRepo.findByStatus("WAIT_AUDIT", PageRequest.of(0, 1)).getTotalElements();
        long members = memberRepo.count();
        List<ProductSku> allSkus = skuRepo.findAll();
        long lowStock = allSkus.stream().filter(s -> s.availableStock() < 10).count();
        return R.ok(Map.of(
                "todaySales", 0,
                "waitShipOrders", waitShip,
                "waitAuditAfterSales", waitAudit,
                "memberCount", members,
                "lowStockSkuCount", lowStock));
    }

    /** M-02 商品列表（含下架） */
    @GetMapping("/products")
    public R<Page<Product>> products(@RequestHeader("X-Token") String token,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        return R.ok(productRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    /** M-02 上下架（AC-08：变更即生效，客户端即时不可见） */
    @PostMapping("/products/{id}/status")
    public R<Void> changeStatus(@RequestHeader("X-Token") String token, @PathVariable Long id, @RequestBody Map<String, Integer> body) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        Product p = productRepo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) throw new BizException("状态值不合法（仅支持 0 下架 / 1 上架）");
        p.setStatus(status);
        productRepo.save(p);
        return R.ok();
    }

    /** M-05 SKU 库存列表 */
    @GetMapping("/skus")
    public R<List<ProductSku>> skus(@RequestHeader("X-Token") String token) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        return R.ok(skuRepo.findAll());
    }

    /** M-05 库存调整（AC-08 留痕：这里以日志形式记录，正式版入 stock_flow 表） */
    @PostMapping("/skus/{id}/stock")
    public R<?> adjustStock(@RequestHeader("X-Token") String token, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        ProductSku sku = skuRepo.findById(id).orElseThrow(() -> new BizException("SKU 不存在"));
        if (body.get("delta") == null) throw new BizException("调整数量不能为空");
        int delta = ((Number) body.get("delta")).intValue();
        if (delta == 0) throw new BizException("调整数量不能为 0");
        String reason = String.valueOf(body.getOrDefault("reason", "人工调整"));
        if (sku.getTotalStock() + delta < 0) throw new BizException("库存不足，无法扣减");
        sku.setTotalStock(sku.getTotalStock() + delta);
        skuRepo.save(sku);
        org.slf4j.LoggerFactory.getLogger("STOCK_FLOW")
                .info("[留痕] SKU {}({}) 变更 {} 原因: {} 变更后可售 {}",
                        sku.getId(), sku.getSpec(), delta, reason, sku.availableStock());
        return R.ok(Map.of("totalStock", sku.getTotalStock(), "availableStock", sku.availableStock()));
    }

    /** M-03 订单列表 */
    @GetMapping("/orders")
    public R<Page<MallOrder>> orders(@RequestHeader("X-Token") String token,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return R.ok(status == null || status.isBlank()
                ? orderRepo.findAll(pr)
                : orderRepo.findByStatus(status, pr));
    }

    /** O-08 发货 */
    @PostMapping("/orders/{orderNo}/ship")
    public R<Void> ship(@RequestHeader("X-Token") String token, @PathVariable String orderNo, @RequestBody Map<String, String> body) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        mallService.ship(orderNo, body.get("logisticsNo"));
        return R.ok();
    }

    /** M-04 售后列表 */
    @GetMapping("/aftersales")
    public R<Page<AfterSale>> aftersales(@RequestHeader("X-Token") String token,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return R.ok(status == null || status.isBlank()
                ? afterSaleRepo.findAll(pr)
                : afterSaleRepo.findByStatus(status, pr));
    }

    /** A-05 审核（通过/拒绝+原因） */
    @PostMapping("/aftersales/{id}/audit")
    public R<AfterSale> audit(@RequestHeader("X-Token") String token, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        return R.ok(mallService.audit(id, pass, (String) body.get("rejectReason")));
    }

    /** A-07 确认收到退货并退款 */
    @PostMapping("/aftersales/{id}/confirm-return")
    public R<AfterSale> confirmReturn(@RequestHeader("X-Token") String token, @PathVariable Long id) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        return R.ok(mallService.confirmReturn(id));
    }

    /** M-07 会员列表 */
    @GetMapping("/members")
    public R<Page<Member>> members(@RequestHeader("X-Token") String token,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        return R.ok(memberRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "growth"))));
    }

    /** M-07 积分人工调整（留痕） */
    @PostMapping("/members/{id}/points")
    public R<?> adjustPoints(@RequestHeader("X-Token") String token, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        if (body.get("delta") == null) throw new BizException("调整数量不能为空");
        int delta = ((Number) body.get("delta")).intValue();
        if (delta == 0) throw new BizException("调整数量不能为 0");
        String reason = String.valueOf(body.getOrDefault("reason", "人工调整"));
        int points = mallService.addPoints(id, delta > 0 ? "EARN" : "REFUND_BACK", delta, "管理员调整: " + reason);
        return R.ok(Map.of("points", points));
    }

    /** 积分明细查询（M-07） */
    @GetMapping("/members/{id}/points/logs")
    public R<Page<PointsLog>> memberPointsLogs(@RequestHeader("X-Token") String token, @PathVariable Long id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        if (check(token).getCode() != 200) return R.unauthorized("管理员未登录");
        return R.ok(pointsLogRepo.findByMemberId(id, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
