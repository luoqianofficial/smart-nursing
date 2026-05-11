package com.smartnursing.smartnursingadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.smartnursing.**.mapper")
@ComponentScan(basePackages = {"com.smartnursing"})
public class SmartNursingAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartNursingAdminApplication.class, args);
    }

}
