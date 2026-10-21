package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.R;
import com.qingguoji.mall.common.TokenStore;
import com.qingguoji.mall.service.MallService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** C-01~C-05 购物车接口 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final TokenStore tokenStore;
    private final MallService mallService;

    @GetMapping
    public R<List<MallService.CartView>> list(@RequestHeader("X-Token") String token) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        return R.ok(mallService.cartView(mid));
    }

    @PostMapping
    public R<Void> add(@RequestHeader("X-Token") String token, @RequestBody AddReq req) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        mallService.addToCart(mid, req.getSkuId(), req.getQty());
        return R.ok();
    }

    @PutMapping("/{cartId}")
    public R<Void> update(@RequestHeader("X-Token") String token, @PathVariable Long cartId, @RequestBody UpdateReq req) {
        Long mid = tokenStore.memberId(token);
        if (mid == null) return R.unauthorized("请先登录");
        mallService.updateCart(mid, cartId, req.getQty());
        return R.ok();
    }

    @Data
    public static class AddReq {
        private Long skuId;
        private Integer qty;
    }

    @Data
    public static class UpdateReq {
        private Integer qty;
    }
}
