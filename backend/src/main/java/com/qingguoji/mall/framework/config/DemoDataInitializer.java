package com.qingguoji.mall.framework.config;

import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.ScanCode;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.repository.ScanCodeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 演示数据初始化（H2 内存库每次重启自动重建）
 *
 * <p>生产环境通过 {@code DEMO_DATA=false} 关闭，改由 SQL 脚本初始化。
 *
 * @author qingguoji
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    /** 演示防伪码数量 */
    private static final int SCAN_CODE_COUNT = 30;

    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ScanCodeRepository scanCodeRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        seed(
                "枸杞青果槟榔 10元装",
                "枸杞系列",
                "匠心卤制 · 枸杞入魂",
                "#C93A32",
                26004,
                "NORMAL2X",
                new Object[][] {{"单包", "10", 500}, {"整条5包", "50", 300}, {"整箱50包", "450", 12}});
        seed(
                "咖啡味金花槟榔 15元装",
                "咖啡系列",
                "香浓咖啡 · 金花入魂",
                "#4A3226",
                18120,
                "NORMAL",
                new Object[][] {{"单包", "15", 540}, {"整条5包", "75", 180}});
        seed(
                "冰榔薄荷爆珠 20元装",
                "冰榔系列",
                "清凉劲爽 · 一颗爆珠",
                "#2F6F5E",
                9200,
                "NORMAL",
                new Object[][] {{"单包", "20", 320}, {"整条5包", "100", 90}});
        seed(
                "精选青果散籽 250g 罐装",
                "散籽专区",
                "颗粒饱满 · 罐装锁鲜",
                "#B8862B",
                4300,
                "NORMAL",
                new Object[][] {{"250g罐装", "88", 5}});
        seed(
                "青果纪黑金礼盒 全系列10包装",
                "礼盒",
                "尊享礼遇 · 送礼首选",
                "#8E1714",
                860,
                "NORMAL2X",
                new Object[][] {{"10包装礼盒", "268", 6}});
        seed(
                "金玉满堂礼盒 枸杞+咖啡",
                "礼盒",
                "中秋团圆礼",
                "#C9912F",
                1204,
                "NORMAL",
                new Object[][] {{"礼盒装", "168", 45}});
        seed(
                "芝麻香爆珠青果 25元装",
                "爆珠系列",
                "芝麻香气 · 双重爆珠",
                "#7A4E9E",
                6700,
                "NORMAL",
                new Object[][] {{"单包", "25", 260}, {"整条5包", "125", 60}});
        seed(
                "话梅味青果槟榔 12元装",
                "青果系列",
                "酸甜话梅 · 回味悠长",
                "#1E6FA8",
                11000,
                "NORMAL",
                new Object[][] {{"单包", "12", 420}, {"整条5包", "60", 150}});
        seed(
                "木糖醇清口青果 30元装",
                "青果系列",
                "木糖醇配方 · 清口无负担",
                "#3D8B37",
                3900,
                "NORMAL",
                new Object[][] {{"单包", "30", 210}});
        seed(
                "经典桂子油槟榔 8元装",
                "经典系列",
                "经典老味 · 湖南味道",
                "#C9782B",
                42000,
                "NORMAL",
                new Object[][] {{"单包", "8", 800}});

        for (int i = 0; i < SCAN_CODE_COUNT; i++) {
            ScanCode scanCode = new ScanCode();
            scanCode.setCode("QGJ" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            scanCode.setUsed(false);
            scanCodeRepository.save(scanCode);
        }
        log.info(
                "=== 演示数据就绪：{} 个商品，{} 个防伪码 ===",
                productRepository.count(),
                scanCodeRepository.count());
        log.info("=== 演示防伪码示例: {} ===", scanCodeRepository.findAll().get(0).getCode());
    }

    /** 建商品 + 规格，skus 每行为 {规格, 单价, 总库存} */
    private void seed(
            String name,
            String category,
            String subtitle,
            String cover,
            int sales,
            String pointsFlag,
            Object[][] skus) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setSubtitle(subtitle);
        product.setCover(cover);
        product.setSales(sales);
        product.setStatus(1);
        product.setPointsFlag(pointsFlag);
        product.setCreatedAt(LocalDateTime.now());
        product = productRepository.save(product);

        for (Object[] row : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(product.getId());
            sku.setSpec((String) row[0]);
            sku.setPrice(new BigDecimal((String) row[1]));
            sku.setTotalStock((Integer) row[2]);
            sku.setLockedStock(0);
            sku.setSkuCode("SKU" + product.getId() + "-" + row[0]);
            productSkuRepository.save(sku);
        }
    }
}
