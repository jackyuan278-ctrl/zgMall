package com.zgmall.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.common.BizException;
import com.zgmall.common.interceptor.UserContext;
import com.zgmall.common.utils.JwtTool;
import com.zgmall.user.domain.dto.ChangePasswordDTO;
import com.zgmall.user.domain.dto.LoginFormDTO;
import com.zgmall.user.domain.dto.RegisterFormDTO;
import com.zgmall.user.domain.dto.UpdateUserDTO;
import com.zgmall.user.domain.po.User;
import com.zgmall.user.domain.vo.UserLoginVO;
import com.zgmall.user.domain.vo.UserVO;
import com.zgmall.user.mapper.UserMapper;
import com.zgmall.user.service.IUserService;
import com.zgmall.user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtTool jwtTool;

    // 容器 Bean：passWordConfig 里 @Bean 注册，@RequiredArgsConstructor 自动注入
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterFormDTO registerFormDTO) {
        String username = registerFormDTO.getUsername();
        if (query().eq("username", username).exists()) {
            throw new BizException(400, "用户名已存在");
        }
        String phone = registerFormDTO.getPhone();
        if (query().eq("phone", phone).exists()) {
            throw new BizException(400, "手机号已存在");
        }
        String password = registerFormDTO.getPassword();
        String code = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(code);
        save(user);
    }

    @Override
    public UserLoginVO login(LoginFormDTO loginFormDTO) {
        String username = loginFormDTO.getUsername();
        User user = lambdaQuery().eq(User::getUsername, username).one();

        // 用户名不存在和密码错误统一报错，防止攻击者枚举账号
        if (user == null || !passwordEncoder.matches(loginFormDTO.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }

        // 校验账号状态
        if (user.getStatus() == UserStatus.DISABLED.getValue()) {
            throw new BizException(400, "账号已禁用");
        }

        // 生成JWT，组装VO
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUsername(user.getUsername());
        userLoginVO.setUserId(user.getId());
        String token = jwtTool.createToken(user.getId());
        userLoginVO.setToken(token);
        return userLoginVO;
    }


    @Override
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        Long userId = UserContext.getUser();
        User user = getById(userId);
        if (user == null) {
            throw new BizException(400, "用户不存在");
        }
        String password = user.getPassword();
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), password)) {
            throw new BizException(400,"原密码输入错误");
        }
        String newPassword = changePasswordDTO.getNewPassword();
        String oldPassword = changePasswordDTO.getOldPassword();
        if (newPassword.equals(oldPassword)) {
            throw new BizException(400,"前后密码一致");
        }
        update().eq("id", userId).set("password", passwordEncoder.encode(newPassword)).update();
    }

    @Override
    public UserVO queryMe() {
        Long userId = UserContext.getUser();
        User user = getById(userId);
        if (user == null) {
            throw new BizException(400, "用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updateMe(UpdateUserDTO updateUserDTO) {
        Long userId = UserContext.getUser();
        User user = getById(userId);
        if (user == null) {
            throw new BizException(400, "用户不存在");
        }
        // 手机号查重（排除自己）
        String phone = updateUserDTO.getPhone();
        if (phone != null && !phone.equals(user.getPhone())
                && query().eq("phone", phone).ne("id", userId).exists()) {
            throw new BizException(400, "手机号已被占用");
        }
        // updateById 配合 update-strategy: not_null，null 字段自动跳过
        User update = new User();
        update.setId(userId);
        update.setNickname(updateUserDTO.getNickname());
        update.setPhone(updateUserDTO.getPhone());
        update.setAvatar(updateUserDTO.getAvatar());
        updateById(update);
    }

    @Override
    public void deductBalance(Long userId, Integer amount) {
        User user = query().eq("id", userId).one();
        if (user == null) {
            throw new BizException("用户不存在");
        }
        boolean deducted = lambdaUpdate()
                .eq(User::getId, userId)
                .ge(User::getBalance, amount)
                .setDecrBy(User::getBalance, amount)
                .update();
        if (!deducted) {
            throw new BizException("余额不足");
        }
    }
}
