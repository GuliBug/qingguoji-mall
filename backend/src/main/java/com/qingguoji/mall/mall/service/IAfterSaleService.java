package com.qingguoji.mall.mall.service;

import com.qingguoji.mall.mall.domain.AfterSale;
import com.qingguoji.mall.mall.domain.dto.AfterSaleBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 售后服务（A-01~A-09）
 *
 * @author qingguoji
 */
public interface IAfterSaleService {

    /** A-02/A-03 申请售后 */
    AfterSale applyAfterSale(Long memberId, AfterSaleBody body);

    /** A-01 我的售后列表 */
    Page<AfterSale> selectMyAfterSalePage(Long memberId, Pageable pageable);

    /** A-07 买家填写退货物流单号 */
    void fillReturnLogistics(Long afterSaleId, Long memberId, String logisticsNo);

    /** A-05/A-06/A-07 商家审核 */
    AfterSale audit(Long afterSaleId, boolean pass, String rejectReason);

    /** A-07 商家确认收到退货并退款 */
    AfterSale confirmReturn(Long afterSaleId);

    /** M-04 管理端售后列表 */
    Page<AfterSale> selectAfterSalePage(String status, Pageable pageable);
}
