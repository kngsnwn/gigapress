package etners.standard.mvc.test.domain.request;

import etners.common.config.swagger.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestFirstAccessInfoRequestParam {

  @Schema(
      example = "ko",
      allowableValues = "ko/en/zh/vn/jp",
      required = false,
      description = "언어 로케일 설정. ko(한국어) / en(영어) / zh(중국어) / vn(베트남어) / jp(일본어) 중 하나의 값이 요청 파라미터로 넘어오면 해당 언어에 대한 메시지 데이터를 응답시 제공한다.",
      hidden = false
  )
  private String locale = "ko";

  @Schema(description = "로그인 토큰", example = SwaggerConstants.DEFAULT_EXAMPLE_EBMP_LOGIN_TOKEN, required = true)
  private String loginUserToken;
}
