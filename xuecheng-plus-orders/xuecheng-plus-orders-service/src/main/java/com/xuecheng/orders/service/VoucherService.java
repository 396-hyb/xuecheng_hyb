package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.VoucherDto;

import java.util.List;

public interface VoucherService {
    List<VoucherDto> queryVoucherOfShop(Long shopId);
    void addVoucher(VoucherDto voucher);

    void addSeckillVoucher(VoucherDto voucher);
}
