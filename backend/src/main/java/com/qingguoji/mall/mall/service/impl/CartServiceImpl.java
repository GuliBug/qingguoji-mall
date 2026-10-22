package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.mall.domain.CartItem;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.vo.CartItemVo;
import com.qingguoji.mall.mall.repository.CartRepository;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.service.ICartService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 购物车服务实现
 *
 * @author qingguoji
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    /** 单次加购上限 */
    private static final int MAX_QTY_PER_ADD = 999;

    private static final int STATUS_ON_SALE = 1;

    private final CartRepository cartRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CartItemVo> selectCartList(Long memberId) {
        List<CartItemVo> result = new ArrayList<>();
        for (CartItem cartItem : cartRepository.findByMemberId(memberId)) {
            ProductSku sku = productSkuRepository.findById(cartItem.getSkuId()).orElse(null);
            if (sku == null) {
                continue;
            }
            Product product = productRepository.findById(sku.getProductId()).orElse(null);
            CartItemVo vo = new CartItemVo();
            vo.setCartId(cartItem.getId());
            vo.setSkuId(sku.getId());
            vo.setQty(cartItem.getQty());
            vo.setSpec(sku.getSpec());
            vo.setPrice(sku.getPrice());
            vo.setProductName(product == null ? "未知商品" : product.getName());
            vo.setCover(product == null ? "#ccc" : product.getCover());
            vo.setOnSale(
                    product != null
                            && product.getStatus() != null
                            && product.getStatus() == STATUS_ON_SALE);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long memberId, Long skuId, Integer qty) {
        if (skuId == null) {
            throw new ServiceException("SKU 不能为空");
        }
        if (qty == null || qty <= 0) {
            throw new ServiceException("购买数量必须大于 0");
        }
        if (qty > MAX_QTY_PER_ADD) {
            throw new ServiceException("单次加购数量不能超过 999");
        }
        ProductSku sku =
                productSkuRepository
                        .findById(skuId)
                        .orElseThrow(() -> new ServiceException("SKU 不存在"));
        Product product =
                productRepository
                        .findById(sku.getProductId())
                        .orElseThrow(() -> new ServiceException("商品不存在"));
        if (product.getStatus() == null || product.getStatus() != STATUS_ON_SALE) {
            throw new ServiceException("商品已下架，无法加入购物车");
        }

        CartItem cartItem =
                cartRepository
                        .findByMemberIdAndSkuId(memberId, skuId)
                        .orElseGet(
                                () -> {
                                    CartItem created = new CartItem();
                                    created.setMemberId(memberId);
                                    created.setSkuId(skuId);
                                    created.setQty(0);
                                    return created;
                                });
        cartItem.setQty(cartItem.getQty() + qty);
        cartRepository.save(cartItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCart(Long memberId, Long cartId, Integer qty) {
        if (qty == null) {
            throw new ServiceException("数量不能为空");
        }
        CartItem cartItem =
                cartRepository.findById(cartId).orElseThrow(() -> new ServiceException("购物车项不存在"));
        if (!cartItem.getMemberId().equals(memberId)) {
            throw new ServiceException("无权操作");
        }
        if (qty <= 0) {
            cartRepository.delete(cartItem);
            return;
        }
        cartItem.setQty(qty);
        cartRepository.save(cartItem);
    }
}
