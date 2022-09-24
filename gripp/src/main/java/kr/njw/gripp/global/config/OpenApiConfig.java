package kr.njw.gripp.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Gripp Api",
                description = "Gripp 애플리케이션 서버 API 명세서",
                version = "v220925"
        )
)
@Configuration
public class OpenApiConfig {
}
