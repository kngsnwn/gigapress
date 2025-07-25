package etners.standard.mvc.test.domain.response;

import etners.common.util.enumType.SolutionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestInsertResponse implements Serializable {

  private static final long serialVersionUID = -3175124847277476476L;

  @Schema(
      description = "솔루션 코드",
      example = "1019"
  )
  private String solCd;

  @Schema(
      description = "그룹 코드",
      example = "100"
  )
  private String cdGrup;
}
