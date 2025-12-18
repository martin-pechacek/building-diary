package cz.mp.building_diary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI buildingDiaryOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Building Diary API")
						.description("""
								API for tracking construction and building project progress.

								**Authentication:** This API uses session-based authentication with Keycloak.
								To access protected endpoints:
								1. Navigate to `/oauth2/authorization/keycloak` to initiate login
								2. After successful authentication, your session cookie will be set automatically
								3. All subsequent API calls will use the session cookie for authentication

								**Roles:**
								- `USER` - Can access `/api/**` endpoints
								- `ADMIN` - Can access `/api/admin/**` endpoints
								""")
						.version("0.0.1-SNAPSHOT"))
				.addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
				.components(new io.swagger.v3.oas.models.Components()
						.addSecuritySchemes("cookieAuth", new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.COOKIE)
								.name("JSESSIONID")
								.description("Session cookie obtained after OAuth2 login via Keycloak")));
	}
}