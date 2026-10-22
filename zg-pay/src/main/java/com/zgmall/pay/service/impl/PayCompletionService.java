package com.zgmall.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zgmall.pay.domain.PayStatus;
import com.zgmall.pay.domain.po.PayEventOutbox;
import com.zgmall.pay.domain.po.PayOrder;
import com.zgmall.pay.mapper.PayEventOutboxMapper;
import com.zgmall.pay.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Atomically records the paid state and the event that Trade must receive. */
@Service
@RequiredArgsConstructor
public class PayCompletionService {
    private final PayOrderMapper payOrderMapper;
    private final PayEventOutboxMapper outboxMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean markPaidAndCreateEvent(Long payOrderId, Long orderId) {
        int updated = payOrderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getId, payOrderId)
                .eq(PayOrder::getStatus, PayStatus.PROCESSING)
                .set(PayOrder::getStatus, PayStatus.PAID)
                .set(PayOrder::getPayTime, LocalDateTime.now()));
        if (updated == 0) return false;
        PayEventOutbox event = new PayEventOutbox();
        event.setBizOrderNo(orderId);
        event.setPublished(0);
        event.setCreateTime(LocalDateTime.now());
        outboxMapper.insert(event);
        return true;
    }
}
