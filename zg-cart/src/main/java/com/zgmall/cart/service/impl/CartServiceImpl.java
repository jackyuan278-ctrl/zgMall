package com.zgmall.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.zgmall.api.client.ItemClient;
import com.zgmall.api.dto.ItemDTO;
import com.zgmall.cart.domain.dto.AddCartDTO;
import com.zgmall.cart.domain.dto.UpdateCartNumDTO;
import com.zgmall.cart.domain.vo.CartItemVO;
import com.zgmall.cart.service.ICartService;
import com.zgmall.common.BizException;
import com.zgmall.common.Result;
import com.zgmall.common.interceptor.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private static final String KEY_PREFIX = "zg:cart:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ItemClient itemClient;

    @Override
    public List<CartItemVO> queryMyCart() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BizException(400,"请先登录");
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(KEY_PREFIX + userId);
        List<CartItemVO> list = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Object valueobj = entry.getValue();
            if (valueobj == null) {
                continue;
            }
            String jsonStr = valueobj.toString();
            try {
                CartItemVO itemVO = JSONUtil.toBean(jsonStr, CartItemVO.class);
                list.add(itemVO);
            } catch (Exception e) {
                log.error("购物车项JSON解析失败，userId:{}, json:{}", userId, jsonStr, e);
            }
        }
        return list;
    }

    @Override
    public void addItem(AddCartDTO addCartDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BizException(400,"请先登录");
        }
        Integer num = addCartDTO.getNum();
        if (num <=0) {
            throw new BizException(400,"加购数量必须大于0");
        }
        Long itemId = addCartDTO.getItemId();
        Object o = stringRedisTemplate.opsForHash().get(KEY_PREFIX + userId, itemId.toString());
        if (o != null) {
            String jsonStr = o.toString();
            try {
                CartItemVO itemVO = JSONUtil.toBean(jsonStr, CartItemVO.class);
                num = itemVO.getNum() +num;
                if (num>99){
                    num = 99;
                }
                itemVO.setItemId(itemId);
                itemVO.setNum(num);
                String newjsonStr = JSONUtil.toJsonStr(itemVO);
                stringRedisTemplate.opsForHash().put(KEY_PREFIX+userId,itemId.toString(),newjsonStr);
                stringRedisTemplate.expire(KEY_PREFIX+userId,30, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("购物车项JSON解析失败，userId:{}, json:{}", userId, jsonStr, e);
            }
        } else {
            Result<ItemDTO> result = itemClient.queryItemById(itemId);
            ItemDTO itemDTO = result == null ? null : result.getData();
            if (itemDTO == null) {
                throw new BizException(400,"货物不存在" );
            }
            if (itemDTO.getStatus() != null && itemDTO.getStatus() == 0) {
                throw new BizException(400,"商品已下架");
            }
            CartItemVO cartItemVO = BeanUtil.copyProperties(itemDTO, CartItemVO.class);
            cartItemVO.setItemId(itemId);
            cartItemVO.setNum(num);
            stringRedisTemplate.opsForHash().put(KEY_PREFIX+userId,itemId.toString(),JSONUtil.toJsonStr(cartItemVO));
            stringRedisTemplate.expire(KEY_PREFIX+userId,30, TimeUnit.DAYS);
        }

    }

    @Override
    public void updateNum(Long itemId, UpdateCartNumDTO updateCartNumDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BizException(400,"请先登录");
        }
        Integer num = updateCartNumDTO.getNum();
        if (num <=0) {
            throw new BizException(400,"加购数量必须大于0");
        }
        if (num>99){
            num = 99;
        }
        Object o = stringRedisTemplate.opsForHash().get(KEY_PREFIX + userId, itemId.toString());
        if (o != null) {
            String jsonStr = o.toString();
            try {
                CartItemVO itemVO = JSONUtil.toBean(jsonStr, CartItemVO.class);
                itemVO.setNum(num);
                String newjsonStr = JSONUtil.toJsonStr(itemVO);
                stringRedisTemplate.opsForHash().put(KEY_PREFIX+userId,itemId.toString(),newjsonStr);
                stringRedisTemplate.expire(KEY_PREFIX+userId,30, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("购物车项JSON解析失败，userId:{}, json:{}", userId, jsonStr, e);
            }
        }else {
            throw new BizException(400, "购物车条目不存在");
        }

    }

    @Override
    public void removeItem(Long itemId) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BizException(400,"请先登录");
        }
        stringRedisTemplate.opsForHash().delete(KEY_PREFIX + userId, itemId.toString());
    }

    @Override
    public void clearCart() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BizException(400,"请先登录");
        }
        stringRedisTemplate.delete(KEY_PREFIX + userId);
    }
}
