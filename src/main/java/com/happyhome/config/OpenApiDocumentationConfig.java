package com.happyhome.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SSAFY Home REST API",
                version = "v1",
                description = "부동산 실거래, 공공임대, 생활권 분석 REST API",
                contact = @Contact(name = "SSAFY Home")
        ),
        servers = @Server(url = "/", description = "Current server")
)
public class OpenApiDocumentationConfig {
}

@Controller
class SwaggerUiForwardController {

    @GetMapping("/swagger-ui/{resource}")
    public String forwardSwaggerUiResource(@PathVariable String resource) {
        return "forward:/webjars/swagger-ui/" + resource;
    }
}
