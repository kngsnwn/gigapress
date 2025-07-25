package common.domains;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CommonRequestParam {

  @Schema(
    example = "ko",
    allowableValues = "ko/en/zh/vn/jp",
    required = false,
    description = "언어 로케일 설정. ko(한국어) / en(영어) / zh(중국어) / vn(베트남어) / jp(일본어) 중 하나의 값이 요청 파라미터로 넘어오면 해당 언어에 대한 메시지 데이터를 응답시 제공한다."
  )
  private String locale;

  @Schema(description = "업무공간 코드",
    example = "10"
  )
  private String workspaceCd;


}
