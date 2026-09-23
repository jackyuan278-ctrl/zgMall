package com.zgmall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zgmall.user.domain.dto.ChangePasswordDTO;
import com.zgmall.user.domain.dto.LoginFormDTO;
import com.zgmall.user.domain.dto.RegisterFormDTO;
import com.zgmall.user.domain.dto.UpdateUserDTO;
import com.zgmall.user.domain.po.User;
import com.zgmall.user.domain.vo.UserLoginVO;
import com.zgmall.user.domain.vo.UserVO;

public interface IUserService extends IService<User> {

    void register(RegisterFormDTO registerFormDTO);

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void changePassword(ChangePasswordDTO changePasswordDTO);

    UserVO queryMe();

    void updateMe(UpdateUserDTO updateUserDTO);

    /** 支付服务扣减余额（Feign 直连） */
    void deductBalance(Long userId, Integer amount);
}
