package common.domains;


import etners.ebmp.lib.api.kendomodel.KendoPagingParamVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "로그인 토큰과 Kendo 페이징 정보를 가지고 컨시어지 배차요청 정보를 목록 형태로 요청하는 파라미터 클래스")
public class KendoRequestParam extends KendoPagingParamVO {

  private static final long serialVersionUID = -3288469576773479233L;

  @Schema(
    example = "ko",
    allowableValues = "ko/en/zh/vn/jp",
    description = "언어 로케일 설정. ko(한국어) / en(영어) / zh(중국어) / vn(베트남어) / jp(일본어) 중 하나의 값이 요청 파라미터로 넘어오면 해당 언어에 대한 메시지 데이터를 응답시 제공한다."
  )
  private String locale;

}
