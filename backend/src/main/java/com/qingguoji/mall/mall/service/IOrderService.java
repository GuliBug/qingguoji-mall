package com.qingguoji.mall.mall.service;

import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.OrderItem;
import com.qingguoji.mall.mall.domain.dto.OrderBody;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 订单服务（O-01~O-08 / S-02~S-04）
 *
 * @author qingguoji
 */
public interface IOrderService {

    /** O-01/O-02 下单：库存预占 + 订单创建（同事务，AC-01 防超卖） */
    MallOrder createOrder(Long memberId, OrderBody body);

    /** O-03 模拟支付：扣减库存 + 发放积分与成长值 */
    MallOrder payOrder(String orderNo, Long memberId);

    /** O-05 取消订单：释放预占库存（幂等） */
    void cancelOrder(String orderNo, Long memberId);

    /** O-07 确认收货 */
    void confirmReceive(String orderNo, Long memberId);

    /** O-08 管理端发货 */
    void shipOrder(String orderNo, String logisticsNo);

    /** O-06 买家订单列表 */
    Page<MallOrder> selectMemberOrderPage(Long memberId, String status, Pageable pageable);

    /** M-03 管理端订单列表 */
    Page<MallOrder> selectOrderPage(String status, Pageable pageable);

    /** 订单明细 */
    List<OrderItem> selectItemsByOrderId(Long orderId);

    /** O-05 超时自动取消，返回取消笔数 */
    int cancelExpiredOrders();
}
