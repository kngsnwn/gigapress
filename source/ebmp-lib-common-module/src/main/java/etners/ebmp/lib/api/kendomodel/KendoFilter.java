package etners.ebmp.lib.api.kendomodel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Schema(description = "Kendo UI에서 전달하는 검색조건 필터 정보를 Querydsl을 활용해 자동화할 수 있도록 하기 위해 전달하는 파라미터 클래스입니다.")
@Data
@EqualsAndHashCode(callSuper = false)
public class KendoFilter implements Serializable {

  private static final long serialVersionUID = -271581726447899346L;

  @Schema(
    example = "email",
    required = true,
    description = "필수값. 검색조건 필터로 설정하려고 하는 DB 테이블 내의 컬럼명을 전달합니다. DB 컬럼명은 언더스코어 형식으로 되어있더라도 카멜케이스 형식으로 전달하면 됩니다."
  )
  private String field;

  @Schema(
    example = "like",
    allowableValues = "between/contains/doesNotContain/eq/isequalsto/ne/isnotequalsto/isnull/isnotnull/isempty/isnotempty/oe/littleorequal/lt/little/goe/greaterorequal/gt/greater/like/notlike",
    required = true,
    description = "필수값. 해당 컬럼에 대해 어떤 방식으로 WHERE절의 조건을 연결지을지를 결정합니다. 예를 들어 email 컬럼에 대해 isempty를 설정하게 되면 쿼리에서 Where절 안에 다음과 같이 내용이 추가됩니다. (ex : WHERE email IS NULL). 이 오퍼레이터에 무슨 값을 넘겨주냐에 따라 널인 값만 찾거나 널이 아닌 값만 찾거나 LIKE 구문으로 포함된 것을 찾거나, 완벽히 일치하는 것을 찾는 eq를 활용할 수도 있고, greater or equal을 써서 조건으로 건 값과 크거나 같은 값(<=)을 조회할 수도 있습니다. 단, between을 이용하려면 이 필터 클래스보다 상위 모델인 KendoFilterModule에서 logic 항목에 between으로 값이 세팅되어야만 합니다."
  )
  private String operator;


  @Schema(
    example = "@etners.com",
    required = true,
    description = "필수값. 검색 조건에서 어떤 값으로 검색할지를 결정합니다. 단 이 값은 다음과 같은 operator가 같이 넘어왔을 때는 사용하지 않습니다. isnull, isnotnull, isempty, isnotempty."
  )
  private String value;


  @Override
  public String toString() {
    return "KendoFilter [" + (field != null ? "field=" + field + ", " : "") + (operator != null ?
      "operator=" + operator + ", " : "") + (value != null ? "value=" + value : "") + "]";
  }

}
