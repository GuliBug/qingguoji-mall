package com.qingguoji.mall.mall.controller;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.service.IProductService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品接口（P-02~P-06，公开访问）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {

    private final IProductService productService;

    /** P-02 列表：仅上架商品可见（AC-07） */
    @GetMapping
    public AjaxResult<Page<Product>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return success(
                productService.selectProductPage(
                        category, buildPageRequest(page, size, "sales", Sort.Direction.DESC)));
    }

    /** P-04/P-05 详情 + SKU 列表（联动价格与库存） */
    @GetMapping("/{id}")
    public AjaxResult<Map<String, Object>> detail(@PathVariable Long id) {
        Product product = productService.selectOnSaleProduct(id);
        List<ProductSku> skus = productService.selectSkuByProductId(id);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("product", product);
        detail.put("skus", skus);
        return success(detail);
    }
}
