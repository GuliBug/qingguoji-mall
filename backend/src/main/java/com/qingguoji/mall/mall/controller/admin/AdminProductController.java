package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.common.core.page.TableDataInfo;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.dto.StatusBody;
import com.qingguoji.mall.mall.service.IProductService;
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
 * 管理端商品管理（M-02）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api/products")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminProductController extends BaseController {

    private final IProductService productService;

    /** M-02 商品列表（含下架） */
    @GetMapping
    public AjaxResult<TableDataInfo<Product>> list(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return getDataTable(
                productService.selectProductPageForAdmin(
                        buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }

    /** M-02 上下架（AC-08：变更即生效，客户端即时不可见） */
    @PostMapping("/{id}/status")
    public AjaxResult<Void> changeStatus(@PathVariable Long id, @RequestBody StatusBody body) {
        productService.changeProductStatus(id, body.getStatus());
        return success();
    }
}
