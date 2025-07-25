package etners.standard.mvc.test.domain.request;

import etners.common.config.swagger.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestInsertRequestParam {

  private static final long serialVersionUID = -3175124847277476476L;

  @Schema(
      example = "ko",
      allowableValues = "ko/en/zh/vn/jp",
      required = false,
      description = "언어 로케일 설정. ko(한국어) / en(영어) / zh(중국어) / vn(베트남어) / jp(일본어) 중 하나의 값이 요청 파라미터로 넘어오면 해당 언어에 대한 메시지 데이터를 응답시 제공한다.",
      hidden = false
  )
  private String locale = "ko";

  @Schema(
      example = SwaggerConstants.DEFAULT_EXAMPLE_EBMP_LOGIN_TOKEN,
      required = true,
      description = "필수값. 사용자의 로그인 토큰. 이 사용자가 이 요청을 하기 적합한 대상인지를 판단하는 근거가 된다.",
      hidden = false
  )
  private String loginUserToken;

  @Schema(
      description = "솔루션 코드",
      example = "1019"
  )
  @NotEmpty
  private String solCd;

  @Schema(
      description = "그룹 코드",
      example = "100"
  )
  @NotEmpty
  private String cdGrup;

  @Schema(
      description = "그룹 코드명",
      example = "테스트"
  )
  @NotBlank
  private String cdGrupNm;

  @Schema(
      description = "코드설명",
      example = "테스트"
  )
  @NotBlank
  private String cdDesc;
}
