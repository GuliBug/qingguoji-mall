package com.qingguoji.mall.mall.controller;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.dto.CartAddBody;
import com.qingguoji.mall.mall.domain.dto.CartUpdateBody;
import com.qingguoji.mall.mall.domain.vo.CartItemVo;
import com.qingguoji.mall.mall.service.ICartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车接口（C-01~C-05）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@RequireLogin(LoginType.MEMBER)
public class CartController extends BaseController {

    private final ICartService cartService;

    /** C-03 购物车列表 */
    @GetMapping
    public AjaxResult<List<CartItemVo>> list() {
        return success(cartService.selectCartList(getMemberId()));
    }

    /** C-01 加入购物车 */
    @PostMapping
    public AjaxResult<Void> add(@RequestBody CartAddBody body) {
        cartService.addCart(getMemberId(), body.getSkuId(), body.getQty());
        return success();
    }

    /** C-02 修改数量 */
    @PutMapping("/{cartId}")
    public AjaxResult<Void> update(@PathVariable Long cartId, @RequestBody CartUpdateBody body) {
        cartService.updateCart(getMemberId(), cartId, body.getQty());
        return success();
    }
}
