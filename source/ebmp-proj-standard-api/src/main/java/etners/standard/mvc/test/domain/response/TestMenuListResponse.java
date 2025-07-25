package etners.standard.mvc.test.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestMenuListResponse implements Serializable {

  private static final long serialVersionUID = -3175124847277476476L;

  @Schema(
      description = "메뉴 ID"
  )
  private String menuId;

  @Schema(
      description = "메뉴 한글명"
  )
  private String menuNm;

  @Schema(
      description = "상위 메뉴 ID"
  )
  private String superMenuId;

  @Schema(
      description = "메뉴 url"
  )
  private String urlLink;
}
