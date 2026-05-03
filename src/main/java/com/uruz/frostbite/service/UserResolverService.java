package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.uruz.frostbite.enums.common.ApiErrorCode.NOT_FOUND;

@Service
public class UserResolverService {
    private UserMapper userMapper;

    public UserResolverService(@Autowired UserMapper userMapper){
        this.userMapper  = userMapper;
    }
    public User getUser(UUID uuid) throws GeneralCustomException {
        return userMapper.selectUser(uuid).orElseThrow(() -> new GeneralCustomException(NOT_FOUND));
    }

}
