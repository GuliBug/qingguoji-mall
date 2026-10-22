package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.common.utils.StringUtils;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.service.IProductService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品与库存服务实现
 *
 * @author qingguoji
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private static final int STATUS_ON_SALE = 1;

    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

    @Override
    public Page<Product> selectProductPage(String category, Pageable pageable) {
        return StringUtils.isEmpty(category)
                ? productRepository.findByStatus(STATUS_ON_SALE, pageable)
                : productRepository.findByCategoryAndStatus(category, STATUS_ON_SALE, pageable);
    }

    @Override
    public Product selectOnSaleProduct(Long productId) {
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ServiceException("商品不存在或已下架"));
        if (product.getStatus() == null || product.getStatus() != STATUS_ON_SALE) {
            throw new ServiceException("商品不存在或已下架");
        }
        return product;
    }

    @Override
    public List<ProductSku> selectSkuByProductId(Long productId) {
        return productSkuRepository.findByProductId(productId);
    }

    @Override
    public Page<Product> selectProductPageForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeProductStatus(Long productId, Integer status) {
        if (status == null || (status != 0 && status != STATUS_ON_SALE)) {
            throw new ServiceException("状态值不合法（仅支持 0 下架 / 1 上架）");
        }
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ServiceException("商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
        log.info("商品 {}({}) 状态变更为 {}", product.getId(), product.getName(), status);
    }

    @Override
    public List<ProductSku> selectSkuList() {
        return productSkuRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adjustStock(Long skuId, Integer delta, String reason) {
        if (delta == null) {
            throw new ServiceException("调整数量不能为空");
        }
        if (delta == 0) {
            throw new ServiceException("调整数量不能为 0");
        }
        ProductSku sku =
                productSkuRepository
                        .findById(skuId)
                        .orElseThrow(() -> new ServiceException("SKU 不存在"));
        int totalStock = sku.getTotalStock() == null ? 0 : sku.getTotalStock();
        if (totalStock + delta < 0) {
            throw new ServiceException("库存不足，无法扣减");
        }
        sku.setTotalStock(totalStock + delta);
        productSkuRepository.save(sku);
        // AC-08 留痕：正式版落 stock_flow 表，当前以独立 logger 记录
        log.info(
                "[留痕] SKU {}({}) 变更 {} 原因: {} 变更后可售 {}",
                sku.getId(),
                sku.getSpec(),
                delta,
                StringUtils.defaultIfBlank(reason, "人工调整"),
                sku.availableStock());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalStock", sku.getTotalStock());
        result.put("availableStock", sku.availableStock());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long skuId, int qty) {
        return productSkuRepository.lockStock(skuId, qty) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long skuId, int qty) {
        if (productSkuRepository.deductStock(skuId, qty) == 0) {
            throw new ServiceException("库存异常，请联系客服");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(Long skuId, int qty) {
        productSkuRepository.releaseStock(skuId, qty);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restock(Long skuId, int qty) {
        productSkuRepository.restock(skuId, qty);
    }
}
