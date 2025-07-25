package etners.standard.mvc.test.domain.response;

import com.fasterxml.jackson.annotation.JsonFilter;
import etners.common.util.annotation.response.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonFilter("jsonFilter")
public class TestListResponse implements Serializable {

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

  @Schema(
      description = "그룹 코드명",
      example = "테스트"
  )
  private String cdGrupNm;

  @Schema(
      description = "그룹 코드명",
      example = "테스트"
  )
  @Mobile
  private String cdGrupAs;
}
