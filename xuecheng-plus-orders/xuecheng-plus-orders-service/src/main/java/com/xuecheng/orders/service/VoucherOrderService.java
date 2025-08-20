package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.VoucherOrderDto;

public interface VoucherOrderService {
    Long seckillVoucher(Long voucherId);

    //创建订单
    // 用于事务调用
    void createVoucherOrder(VoucherOrderDto voucherOrder);
}
