package com.qingguoji.mall.mall.controller;

import com.qingguoji.mall.common.core.controller.BaseController;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.mall.domain.AfterSale;
import com.qingguoji.mall.mall.domain.dto.AfterSaleBody;
import com.qingguoji.mall.mall.domain.dto.LogisticsBody;
import com.qingguoji.mall.mall.service.IAfterSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端售后接口（A-01~A-09）
 *
 * @author qingguoji
 */
@RestController
@RequestMapping("/api/aftersale")
@RequiredArgsConstructor
@RequireLogin(LoginType.MEMBER)
public class AfterSaleController extends BaseController {

    private final IAfterSaleService afterSaleService;

    /** A-02/A-03 申请售后 */
    @PostMapping
    public AjaxResult<AfterSale> apply(@RequestBody AfterSaleBody body) {
        return success(afterSaleService.applyAfterSale(getMemberId(), body));
    }

    /** A-01 我的售后列表（H5 端按 Spring Page 的 content 字段消费，勿改 TableDataInfo） */
    @GetMapping("/my")
    public AjaxResult<Page<AfterSale>> my(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return success(
                afterSaleService.selectMyAfterSalePage(
                        getMemberId(),
                        buildPageRequest(page, size, "createdAt", Sort.Direction.DESC)));
    }

    /** A-07 买家填写退货物流单号 */
    @PostMapping("/{id}/return-logistics")
    public AjaxResult<Void> fillLogistics(@PathVariable Long id, @RequestBody LogisticsBody body) {
        afterSaleService.fillReturnLogistics(id, getMemberId(), body.getLogisticsNo());
        return success();
    }
}
