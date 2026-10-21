package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.R;
import com.qingguoji.mall.common.TokenStore;
import com.qingguoji.mall.domain.MallOrder;
import com.qingguoji.mall.repo.OrderRepo;
import com.qingguoji.mall.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/** O-01~O-07 订单接口 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TokenStore tokenStore;
    private final MallService mallService;
    private final OrderRepo orderRepo;

    /** O-01/O-02 提交订单（预占库存） */
    @PostMapping
    public R<MallOrder> create(@RequestHeader("X-Token") String token, @RequestBody MallService.OrderReq req) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        MallOrder order = mallService.createOrder(mid, req.lines, req.receiver, req.receiverPhone, req.address);
        return R.ok(order);
    }

    /** O-03 模拟支付（扣减库存+发积分） */
    @PostMapping("/{orderNo}/pay")
    public R<MallOrder> pay(@RequestHeader("X-Token") String token, @PathVariable String orderNo) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        return R.ok(mallService.pay(orderNo, mid));
    }

    /** O-05 买家取消（释放库存） */
    @PostMapping("/{orderNo}/cancel")
    public R<Void> cancel(@RequestHeader("X-Token") String token, @PathVariable String orderNo) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        mallService.cancel(orderNo, mid);
        return R.ok();
    }

    /** O-07 确认收货 */
    @PostMapping("/{orderNo}/confirm")
    public R<Void> confirm(@RequestHeader("X-Token") String token, @PathVariable String orderNo) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        mallService.confirmReceive(orderNo, mid);
        return R.ok();
    }

    /** O-06 订单列表 */
    @GetMapping
    public R<Page<MallOrder>> list(@RequestHeader("X-Token") String token,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MallOrder> result = status == null || status.isBlank()
                ? orderRepo.findByMemberId(mid, pr)
                : orderRepo.findByMemberIdAndStatus(mid, status, pr);
        return R.ok(result);
    }
}
