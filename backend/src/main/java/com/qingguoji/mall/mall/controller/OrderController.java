package com.qingguoji.mall.mall.controller;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.dto.OrderBody;
import com.qingguoji.mall.mall.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口（O-01~O-07）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@RequireLogin(LoginType.MEMBER)
public class OrderController extends BaseController {

    private final IOrderService orderService;

    /** O-01/O-02 提交订单（预占库存） */
    @PostMapping
    public AjaxResult<MallOrder> create(@RequestBody OrderBody body) {
        return success(orderService.createOrder(getMemberId(), body));
    }

    /** O-03 模拟支付（扣减库存 + 发积分） */
    @PostMapping("/{orderNo}/pay")
    public AjaxResult<MallOrder> pay(@PathVariable String orderNo) {
        return success(orderService.payOrder(orderNo, getMemberId()));
    }

    /** O-05 买家取消（释放库存） */
    @PostMapping("/{orderNo}/cancel")
    public AjaxResult<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo, getMemberId());
        return success();
    }

    /** O-07 确认收货 */
    @PostMapping("/{orderNo}/confirm")
    public AjaxResult<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceive(orderNo, getMemberId());
        return success();
    }

    /** O-06 订单列表 */
    @GetMapping
    public AjaxResult<Page<MallOrder>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return success(
                orderService.selectMemberOrderPage(
                        getMemberId(),
                        status,
                        buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }
}
