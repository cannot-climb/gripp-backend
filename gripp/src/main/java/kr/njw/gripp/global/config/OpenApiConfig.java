package kr.njw.gripp.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "accessToken",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityScheme(
        name = "admin",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Gripp API",
                description = "Gripp 애플리케이션 서버 API 명세서",
                version = "v1"
        ),
        servers = {
                @Server(url = "/", description = "Default API Server")
        }
)
@Configuration
public class OpenApiConfig {
}
