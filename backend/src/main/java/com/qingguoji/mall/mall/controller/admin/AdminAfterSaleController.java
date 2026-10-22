package com.qingguoji.mall.mall.controller.admin;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.common.core.page.TableDataInfo;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.AfterSale;
import com.qingguoji.mall.mall.domain.dto.AuditBody;
import com.qingguoji.mall.mall.service.IAfterSaleService;
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
 * 管理端售后管理（M-04 / A-05 / A-07）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/admin-api/aftersales")
@RequiredArgsConstructor
@RequireLogin(LoginType.ADMIN)
public class AdminAfterSaleController extends BaseController {

    private final IAfterSaleService afterSaleService;

    /** M-04 售后列表 */
    @GetMapping
    public AjaxResult<TableDataInfo<AfterSale>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return getDataTable(
                afterSaleService.selectAfterSalePage(
                        status, buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }

    /** A-05 审核（通过 / 拒绝 + 原因） */
    @PostMapping("/{id}/audit")
    public AjaxResult<AfterSale> audit(@PathVariable Long id, @RequestBody AuditBody body) {
        return success(
                afterSaleService.audit(
                        id, Boolean.TRUE.equals(body.getPass()), body.getRejectReason()));
    }

    /** A-07 确认收到退货并退款 */
    @PostMapping("/{id}/confirm-return")
    public AjaxResult<AfterSale> confirmReturn(@PathVariable Long id) {
        return success(afterSaleService.confirmReturn(id));
    }
}
