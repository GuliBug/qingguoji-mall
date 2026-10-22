package com.qingguoji.mall.mall.domain.dto;

import java.util.List;
import lombok.Data;

/**
 * 提交订单请求体（O-01）
 *
 * @author qingguoji
 */
@Data
public class OrderBody {

    private List<OrderItemBody> lines;

    private String receiver;

    private String receiverPhone;

    private String address;
}
