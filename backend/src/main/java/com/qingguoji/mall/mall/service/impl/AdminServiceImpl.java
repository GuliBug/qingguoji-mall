package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.framework.web.service.TokenService;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.repository.AfterSaleRepository;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.OrderRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.service.IAdminService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * 管理端通用服务实现
 *
 * @author qingguoji
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    /** 低库存预警阈值 */
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final TokenService tokenService;
    private final OrderRepository orderRepository;
    private final AfterSaleRepository afterSaleRepository;
    private final MemberRepository memberRepository;
    private final ProductSkuRepository productSkuRepository;

    @Override
    public String login(String username, String password) {
        return tokenService.adminLogin(username, password);
    }

    @Override
    public Map<String, Object> selectDashboard() {
        long waitShipOrders =
                orderRepository.findByStatus("PAID", PageRequest.of(0, 1)).getTotalElements();
        long waitAuditAfterSales =
                afterSaleRepository
                        .findByStatus("WAIT_AUDIT", PageRequest.of(0, 1))
                        .getTotalElements();
        long memberCount = memberRepository.count();
        List<ProductSku> allSkus = productSkuRepository.findAll();
        long lowStockSkuCount =
                allSkus.stream().filter(sku -> sku.availableStock() < LOW_STOCK_THRESHOLD).count();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        // TODO 今日销售额待接入支付流水表，当前演示环境固定 0
        dashboard.put("todaySales", 0);
        dashboard.put("waitShipOrders", waitShipOrders);
        dashboard.put("waitAuditAfterSales", waitAuditAfterSales);
        dashboard.put("memberCount", memberCount);
        dashboard.put("lowStockSkuCount", lowStockSkuCount);
        return dashboard;
    }
}
