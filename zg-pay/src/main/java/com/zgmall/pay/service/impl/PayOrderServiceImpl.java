package com.zgmall.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.api.client.TradeClient;
import com.zgmall.api.client.UserClient;
import com.zgmall.api.constants.MqConstants;
import com.zgmall.api.dto.OrderDTO;
import com.zgmall.common.BizException;
import com.zgmall.common.Result;
import com.zgmall.common.interceptor.UserContext;
import com.zgmall.pay.domain.dto.PayFormDTO;
import com.zgmall.pay.domain.po.PayOrder;
import com.zgmall.pay.mapper.PayOrderMapper;
import com.zgmall.pay.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {
    private final TradeClient tradeClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, PayFormDTO payFormDTO) {
        Long userId = UserContext.getUser();

        PayOrder payOrder = lambdaQuery().eq(PayOrder::getBizOrderNo, orderId).one();
        Result<OrderDTO> orderDTOResult = tradeClient.queryOrderById(orderId);
        if (orderDTOResult == null || orderDTOResult.getData() == null) {
            throw new BizException(400, "订单不存在或已关闭");
        }
        OrderDTO data = orderDTOResult.getData();

        Integer status = data.getStatus();
        if (status ==2)  {
            return;
        }
        if (status != 1) {
            throw new BizException(400,"订单不存在或者已关闭");
        }
        if (payOrder != null && payOrder.getStatus() == 2) {
            return; // 幂等
        }

        if (payOrder == null ) {

            payOrder = new PayOrder();
            payOrder.setBizOrderNo(orderId);
            payOrder.setStatus(1);          // 先以未支付落库
            payOrder.setAmount(data.getTotalFee());
            payOrder.setPayUserId(userId);
            payOrder.setCreateTime(LocalDateTime.now());
            save(payOrder);
        }
        if (Objects.equals(payFormDTO.getPayType(), "balance")){
            Result<Void> voidResult = userClient.deductBalance(userId, data.getTotalFee());
            if (voidResult == null || !Objects.equals(voidResult.getCode(), 200)) {
                throw new BizException(400, voidResult == null ? "扣款失败" : voidResult.getMsg());
            }
        }else if(!"mock".equals(payFormDTO.getPayType())){
            throw new BizException(400,"未知付款方式");
        }
        boolean updated = lambdaUpdate()
                .eq(PayOrder::getId, payOrder.getId())
                .eq(PayOrder::getStatus, 1)
                .set(PayOrder::getStatus, 2)
                .set(PayOrder::getPayTime, LocalDateTime.now())
                .update();
        if (!updated) return;



        rabbitTemplate.convertAndSend(MqConstants.PAY_NOTIFY_EXCHANGE,MqConstants.PAY_NOTIFY_KEY,payOrder.getBizOrderNo());

    }
}
