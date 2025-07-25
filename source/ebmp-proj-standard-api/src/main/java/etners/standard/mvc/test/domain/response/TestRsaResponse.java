package etners.standard.mvc.test.domain.response;

import etners.common.util.annotation.masking.Mask;
import etners.common.util.enumType.MaskingType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestRsaResponse {

  private String userId;

  @Mask(type = MaskingType.NAME)
  private String empNm;

  private String email;

  @Mask(type = MaskingType.SP_TEL_NO)
  private String spTelNo;

}
