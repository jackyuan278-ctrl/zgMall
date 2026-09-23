package com.zgmall.cart.service.impl;

import com.zgmall.cart.domain.dto.AddCartDTO;
import com.zgmall.cart.domain.dto.UpdateCartNumDTO;
import com.zgmall.cart.domain.vo.CartItemVO;
import com.zgmall.cart.service.ICartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    // TODO 核心业务待用户实现：注入 StringRedisTemplate（Redis db0 已配好）
    //      购物车存 Redis Hash：key = zg:cart:{userId}（userId 取 UserContext），field = itemId，value = 快照 JSON

    @Override
    public List<CartItemVO> queryMyCart() {
        // TODO 核心业务待用户实现：读 Hash 全量 -> 反序列化快照 JSON -> 列表
        throw new UnsupportedOperationException("TODO: 购物车查询待实现");
    }

    @Override
    public void addItem(AddCartDTO addCartDTO) {
        // TODO 核心业务待用户实现：加购快照（商品信息可由 item 服务 Feign 查询后拼快照）
        //      已存在则 num 累加（上限 99），不存在则写入
        throw new UnsupportedOperationException("TODO: 加购待实现");
    }

    @Override
    public void updateNum(Long itemId, UpdateCartNumDTO updateCartNumDTO) {
        // TODO 核心业务待用户实现：更新快照数量（校验条目存在且属于当前用户）
        throw new UnsupportedOperationException("TODO: 修改数量待实现");
    }

    @Override
    public void removeItem(Long itemId) {
        // TODO 核心业务待用户实现：HDEL 单个条目
        throw new UnsupportedOperationException("TODO: 移除条目待实现");
    }

    @Override
    public void clearCart() {
        // TODO 核心业务待用户实现：DEL 整个 key
        throw new UnsupportedOperationException("TODO: 清空购物车待实现");
    }
}
