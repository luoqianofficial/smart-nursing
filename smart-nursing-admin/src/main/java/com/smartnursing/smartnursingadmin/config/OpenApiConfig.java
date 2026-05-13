package com.smartnursing.smartnursingadmin.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智慧护理系统 API 文档")
                        .version("1.0.0")
                        .description("智慧护理系统后端接口文档，包含用户管理、数字资源管理等模块")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@smartnursing.com")
                                .url("https://github.com/smartnursing"))
                        .license(new License().name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("http://your-domain.com").description("生产环境")
                ));
    }
}
