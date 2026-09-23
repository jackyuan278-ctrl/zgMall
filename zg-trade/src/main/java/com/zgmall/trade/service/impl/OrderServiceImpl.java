package com.zgmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.trade.domain.dto.OrderFormDTO;
import com.zgmall.trade.domain.po.Order;
import com.zgmall.trade.domain.vo.OrderVO;
import com.zgmall.trade.mapper.OrderMapper;
import com.zgmall.trade.service.IOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Override
    public OrderVO createOrder(OrderFormDTO orderFormDTO) {
        // TODO 核心业务待用户实现（面试重头戏）：
        //      1. userId 取 UserContext
        //      2. 逐件调 ItemClient.queryItemById 校验商品存在且上架，以服务端实时价重算 totalFee
        //         （不采信前端 price，防改包篡改价格）
        //      3. 调 ItemClient.deductStock 预扣库存
        //      4. 本地事务写 tb_order（地址快照，status=1）+ tb_order_detail（商品快照）
        //      5. 发送延迟消息（trade.delay.direct / order.delay，TTL 队列超时关单）
        //      注意：Feign 扣库存成功但本地事务失败时要补偿回滚库存
        throw new UnsupportedOperationException("TODO: 创建订单待实现");
    }

    @Override
    public List<OrderVO> queryMyOrders() {
        // TODO 核心业务待用户实现：按 UserContext 查订单（create_time 倒序）+ 拼明细 goods
        throw new UnsupportedOperationException("TODO: 订单列表待实现");
    }

    @Override
    public OrderVO queryOrderById(Long id) {
        // TODO 核心业务待用户实现：查订单 + 明细，校验归属当前用户（防越权）
        throw new UnsupportedOperationException("TODO: 订单详情待实现");
    }

    @Override
    public void markPaySuccess(Long orderId) {
        // TODO 核心业务待用户实现：订单 1 -> 2（条件更新防并发），写 pay_time；触发销量累加（方案待拍板）
        throw new UnsupportedOperationException("TODO: 支付成功处理待实现");
    }

    @Override
    public void closeOrder(Long orderId) {
        // TODO 核心业务待用户实现：仅关 status=1 的订单（条件更新）-> 写 close_time -> ItemClient.restoreStock 回滚库存
        throw new UnsupportedOperationException("TODO: 超时关单待实现");
    }
}
