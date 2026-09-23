package com.zgmall.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zgmall.pay.domain.dto.PayFormDTO;
import com.zgmall.pay.domain.po.PayOrder;

public interface IPayOrderService extends IService<PayOrder> {

    void payOrder(Long orderId, PayFormDTO payFormDTO);
}
