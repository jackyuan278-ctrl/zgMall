package com.zgmall.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.pay.domain.dto.PayFormDTO;
import com.zgmall.pay.domain.po.PayOrder;
import com.zgmall.pay.mapper.PayOrderMapper;
import com.zgmall.pay.service.IPayOrderService;
import org.springframework.stereotype.Service;

@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    @Override
    public void payOrder(Long orderId, PayFormDTO payFormDTO) {
        // TODO 核心业务待用户实现（面试点：幂等 + 跨库 + MQ）：
        //      1. 查/建支付单：biz_order_no 唯一键保证一单一支付单；已支付直接返回成功（幂等）
        //      2. 支付方式分流：
        //         balance -> 调 UserClient.deductBalance 扣余额（跨库，user 侧条件更新防透支）
        //         mock    -> 直接视为支付成功（demo 语义）
        //      3. 更新支付单状态（1 -> 2）+ 写 pay_time
        //      4. 发 MQ pay.success 通知 trade 服务改订单状态
        //      注意：金额以支付单 amount（=订单 totalFee）为准，不采信前端
        throw new UnsupportedOperationException("TODO: 模拟支付待实现");
    }
}
