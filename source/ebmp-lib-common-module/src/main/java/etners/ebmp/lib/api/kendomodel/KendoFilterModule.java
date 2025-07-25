package etners.ebmp.lib.api.kendomodel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Arrays;

@Schema(description = "Kendo UI에서 전달하는 검색조건 필터 정보를 Querydsl을 활용해 자동화할 수 있도록 하기 위해 전달하는 KendoFilter 클래스를 모듈 형태로 래핑하는 클래스입니다. 모든 KendoFilter는 그 값이 하나 뿐이더라도 모두 KendoFilterModule 안에 래핑되어 전달됩니다. 현재 벡엔드 로직은 logic이 between인 경우에만 KendoFilter가 2개 포함되어 사용할 수 있도록 구현되어 있습니다.")
public class KendoFilterModule implements Serializable {

  private static final long serialVersionUID = 1659568936928284670L;

  @Schema(
    example = "and",
    allowableValues = "and/or/between",
    required = true,
    description = "필수값. 이 값은 WHERE절의 조건을 생성할 때 어떤 식으로 값을 연결할지 결정합니다. and인 경우는 AND 조건문으로 생성하고, or일 때는 OR 조건문으로 생성합니다. between의 경우 AND 조건문 안에서 BETWEEN 구문을 이용합니다."
  )
  private String logic;

  private KendoFilter[] filters;

  public KendoFilterModule() {
    super();
  }

  public KendoFilterModule(String logic, KendoFilter[] filters) {
    super();
    this.logic = logic;
    this.filters = filters;
  }

  public KendoFilterModule(KendoFilter... filters) {
    super();
    this.logic = "and";
    this.filters = filters;
  }

  public String getLogic() {
    return logic;
  }

  public void setLogic(String logic) {
    this.logic = logic;
  }

  public KendoFilter[] getFilters() {
    return filters;
  }

  public void setFilters(KendoFilter[] filters) {
    this.filters = filters;
  }

  @Override
  public String toString() {
    return "KendoFilterModule [" + (logic != null ? "logic=" + logic + ", " : "") + (filters != null
      ? "filters=" + Arrays.toString(filters) : "") + "]";
  }

}
