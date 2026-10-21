package com.qingguoji.mall.config;

import com.qingguoji.mall.service.MallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * O-05 订单超时自动取消（AC-02）：每 30 秒扫描一次 15 分钟未支付订单。
 * 正式版建议替换为 Redis Key 过期监听（见 spec 5.3 时序图）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.order-timeout-scan", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutScheduler {

    private final MallService mallService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void cancelExpired() {
        int n = mallService.cancelExpiredOrders();
        if (n > 0) log.info("[超时取消] 自动取消超时未支付订单 {} 笔，库存已释放", n);
    }
}
