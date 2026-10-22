package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.common.core.page.TableDataInfo;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.dto.LogisticsBody;
import com.qingguoji.mall.mall.service.IOrderService;
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
 * 管理端订单管理（M-03 / O-08）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api/orders")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminOrderController extends BaseController {

    private final IOrderService orderService;

    /** M-03 订单列表 */
    @GetMapping
    public AjaxResult<TableDataInfo<MallOrder>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return getDataTable(
                orderService.selectOrderPage(
                        status, buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }

    /** O-08 发货 */
    @PostMapping("/{orderNo}/ship")
    public AjaxResult<Void> ship(@PathVariable String orderNo, @RequestBody LogisticsBody body) {
        orderService.shipOrder(orderNo, body.getLogisticsNo());
        return success();
    }
}
