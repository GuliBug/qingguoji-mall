package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.dto.AdjustBody;
import com.qingguoji.mall.mall.service.IProductService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端库存管理（M-05）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api/skus")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminStockController extends BaseController {

    private final IProductService productService;

    /** M-05 SKU 库存列表 */
    @GetMapping
    public AjaxResult<List<ProductSku>> list() {
        return success(productService.selectSkuList());
    }

    /** M-05 库存调整（AC-08 留痕） */
    @PostMapping("/{id}/stock")
    public AjaxResult<Map<String, Object>> adjustStock(
            @PathVariable Long id, @RequestBody AdjustBody body) {
        return success(productService.adjustStock(id, body.getDelta(), body.getReason()));
    }
}
