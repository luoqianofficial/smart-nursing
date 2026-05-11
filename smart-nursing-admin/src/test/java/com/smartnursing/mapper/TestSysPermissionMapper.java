package com.smartnursing.mapper;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestSysPermissionMapper {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Test
    public void testGetPermCodeByUserId(){
        String permCodeByUserId = sysPermissionMapper.getPermCodeByUserId(1L);
        System.out.println(permCodeByUserId);
    }


}
