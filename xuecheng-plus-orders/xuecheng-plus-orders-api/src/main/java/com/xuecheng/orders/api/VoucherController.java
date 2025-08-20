package com.xuecheng.orders.api;

import com.xuecheng.orders.model.dto.VoucherDto;
import com.xuecheng.orders.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/voucher")
public class VoucherController {
    @Resource
    private VoucherService voucherService;

    /**
     * 新增秒杀券
     * @param voucherDto 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Long addSeckillVoucher(@RequestBody VoucherDto voucherDto) {
        voucherService.addSeckillVoucher(voucherDto);
        return voucherDto.getId();
    }

    /**
     * 新增普通券
     * @param voucherDto 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Long addVoucher(@RequestBody VoucherDto voucherDto) {
        voucherService.addVoucher(voucherDto);
        return voucherDto.getId();
    }


    /**
     * 查询店铺的优惠券列表
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public List<VoucherDto> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }
}
