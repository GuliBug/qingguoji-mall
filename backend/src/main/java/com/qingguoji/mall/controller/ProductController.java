package com.qingguoji.mall.controller;

import com.qingguoji.mall.common.R;
import com.qingguoji.mall.domain.Product;
import com.qingguoji.mall.domain.ProductSku;
import com.qingguoji.mall.repo.ProductRepo;
import com.qingguoji.mall.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** P-02~P-06 商品接口 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepo productRepo;
    private final MallService mallService;

    /** P-02 列表：仅上架商品可见（AC-07） */
    @GetMapping
    public R<Page<Product>> list(@RequestParam(required = false) String category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return R.ok(mallService.products(category, page, size));
    }

    /** P-04/P-05 详情 + SKU 列表（联动价格与库存） */
    @GetMapping("/{id}")
    public R<?> detail(@PathVariable Long id) {
        Product p = productRepo.findById(id).orElse(null);
        if (p == null || p.getStatus() != 1) return R.fail("商品不存在或已下架");
        List<ProductSku> skus = mallService.skusOf(id);
        return R.ok(Map.of("product", p, "skus", skus));
    }
}
