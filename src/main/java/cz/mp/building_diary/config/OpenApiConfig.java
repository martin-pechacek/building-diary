package cz.mp.building_diary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI buildingDiaryOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Building Diary API")
						.description("API for tracking construction and building project progress")
						.version("0.0.1-SNAPSHOT"));
	}
}