package com.qingguoji.mall.mall.service;

import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 商品与库存服务（P-01~P-06 / S-01~S-07）
 *
 * @author qingguoji
 */
public interface IProductService {

    /** P-02 客户端商品列表：仅上架商品（AC-07） */
    Page<Product> selectProductPage(String category, Pageable pageable);

    /** P-04 商品详情，不存在或已下架抛业务异常 */
    Product selectOnSaleProduct(Long productId);

    /** P-05 商品规格列表 */
    List<ProductSku> selectSkuByProductId(Long productId);

    /** M-02 管理端商品列表（含下架） */
    Page<Product> selectProductPageForAdmin(Pageable pageable);

    /** M-02 上下架（AC-08：变更即生效） */
    void changeProductStatus(Long productId, Integer status);

    /** M-05 全量 SKU 列表 */
    List<ProductSku> selectSkuList();

    /** M-05 库存调整（留痕） */
    Map<String, Object> adjustStock(Long skuId, Integer delta, String reason);

    /** S-02 预占库存，成功返回 true */
    boolean lockStock(Long skuId, int qty);

    /** S-03 支付扣减库存，失败抛业务异常 */
    void deductStock(Long skuId, int qty);

    /** S-04 释放预占 */
    void releaseStock(Long skuId, int qty);

    /** S-07 售后回补库存 */
    void restock(Long skuId, int qty);
}
