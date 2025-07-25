package etners.common.config.swagger;

import etners.common.util.annotation.response.Mobile;
import etners.common.util.annotation.response.Web;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
@OpenAPIDefinition(
    info =
    @Info(
        title = "EBMP PROJ STANDARD API",
        description = "Etners Proj standard API 문서입니다.",
        version = "1.0.0",
        license = @License(
            name = "EtnersRnd",
            url = "http://git.etners.com:10114/rnd/ebmp-web-proj-standard-api.git"
        )))
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    String jwtSchemeName = "JWT AccessToken";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
    Components components = new Components()
      .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
        .name(jwtSchemeName)
        .type(SecurityScheme.Type.HTTP)
        .description("Authorization 헤더의 JWT 토큰 값을 입력해 주세요")
        .scheme("bearer"));

    return new OpenAPI()
      .addSecurityItem(securityRequirement)
      .components(components);
  }

  @Bean
  public GroupedOpenApi webApi() {
    return GroupedOpenApi
      .builder()
      .group("WEB")
      .addOperationCustomizer((operation, handlerMethod) -> isWeb(handlerMethod) ? filterMediaTypes(operation, "mobile", "모바일") : null)
      .addOpenApiCustomizer(openApi -> filterSchemaByNameContains(openApi, "mobile"))
      .build();
  }

  @Bean
  public GroupedOpenApi mobileApi() {
    return GroupedOpenApi
      .builder()
      .group("MOBILE")
      .addOperationCustomizer((operation, handlerMethod) -> isMobile(handlerMethod) ? filterMediaTypes(operation, "web", "웹") : null)
      .addOpenApiCustomizer(openApi -> filterSchemaByNameContains(openApi, "web"))
      .build();
  }

  private static Operation filterMediaTypes(Operation operation, String... lowerCaseMediaTypes) {
    ApiResponses responses = operation.getResponses();
    ApiResponse response = responses.get("200");
    if (ObjectUtils.isNotEmpty(response)) {
      Content content = response.getContent();
      if (ObjectUtils.isNotEmpty(content)) {
        content.keySet().removeIf(mediaType -> {
          for (String type : lowerCaseMediaTypes) {
            if (mediaType.toLowerCase().contains(type)) {
              return true;
            }
          }
          return false;
        });
      }
    }
    return operation;
  }

  private static void filterSchemaByNameContains(OpenAPI openApi, String... excludeClassNamePatterns) {
    Map<String, Schema> schemas = openApi.getComponents().getSchemas();
    schemas.keySet().removeIf(schemaName -> {
      String name = schemas.get(schemaName).getName();
      for (String excludeName : excludeClassNamePatterns) {
        if (name.toLowerCase().contains(excludeName)) {
          return true;
        }
      }
      return false;
    });
  }

  private boolean isWeb(HandlerMethod handlerMethod) {
    return (handlerMethod.getBeanType().isAnnotationPresent(Web.class) && !handlerMethod.getBeanType().getAnnotation(Web.class).exclude()) || (handlerMethod.getMethod().isAnnotationPresent(Web.class) && !handlerMethod.getMethod().getAnnotation(Web.class).exclude());
  }

  private boolean isMobile(HandlerMethod handlerMethod) {
    return (handlerMethod.getBeanType().isAnnotationPresent(Mobile.class) && !handlerMethod.getBeanType().getAnnotation(Mobile.class).exclude()) || (handlerMethod.getMethod().isAnnotationPresent(Mobile.class) && !handlerMethod.getMethod().getAnnotation(Mobile.class).exclude());
  }
}
