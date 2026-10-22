package com.qingguoji.mall.framework.task;

import com.qingguoji.mall.mall.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动取消（O-05 / AC-02）
 *
 * <p>每 30 秒扫描一次超过 15 分钟未支付的订单并释放预占库存。 正式环境建议改为 Redis Key 过期监听（见 spec 5.3 时序图）。
 *
 * @author qingguoji
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.order-timeout-scan", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutTask {

    private final IOrderService orderService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void cancelExpiredOrders() {
        int count = orderService.cancelExpiredOrders();
        if (count > 0) {
            log.info("[超时取消] 自动取消超时未支付订单 {} 笔，库存已释放", count);
        }
    }
}
