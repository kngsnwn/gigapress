package etners.standard.mvc.test.domain.request;

import etners.ebmp.lib.api.kendomodel.KendoPagingParamVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestListRequestParam extends KendoPagingParamVO {

  @Serial
  private static final long serialVersionUID = -3175124847277476476L;

  @Schema(
    description = "솔루션 코드(검색조건은 search를 붙인다.)",
    example = "1019"
  )
  private String searchSolCd;

  @Schema(
    description = "복합 검색",
    example = "김김"
  )
  private String searchCompoundText;
}
