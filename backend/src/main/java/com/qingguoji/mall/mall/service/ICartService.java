package com.qingguoji.mall.mall.service;

import com.qingguoji.mall.mall.domain.vo.CartItemVo;
import java.util.List;

/**
 * 购物车服务（C-01~C-05）
 *
 * @author qingguoji
 */
public interface ICartService {

    /** C-03 购物车列表（联表取商品信息） */
    List<CartItemVo> selectCartList(Long memberId);

    /** C-01 加入购物车：同一 SKU 合并数量 */
    void addCart(Long memberId, Long skuId, Integer qty);

    /** C-02 修改数量，数量为 0 时删除 */
    void updateCart(Long memberId, Long cartId, Integer qty);
}
