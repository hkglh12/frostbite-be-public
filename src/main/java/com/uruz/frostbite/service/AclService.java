package com.uruz.frostbite.service;

import com.uruz.frostbite.mapper.AclMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AclService {
    private AclMapper aclMapper;

    @Autowired
    public AclService(AclMapper aclMapper){
        this.aclMapper = aclMapper;
    }
}
