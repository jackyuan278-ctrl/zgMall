package com.zgmall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zgmall.user.domain.dto.AddressDTO;
import com.zgmall.user.domain.po.Address;
import com.zgmall.user.domain.vo.AddressVO;

import java.util.List;

public interface IAddressService extends IService<Address> {

    List<AddressVO> queryMyAddresses();

    void saveAddress(AddressDTO addressDTO);

    void updateAddress(Long id, AddressDTO addressDTO);

    void removeAddress(Long id);

    void setDefault(Long id);
}
