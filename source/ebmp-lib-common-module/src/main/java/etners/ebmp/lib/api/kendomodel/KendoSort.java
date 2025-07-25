package etners.ebmp.lib.api.kendomodel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Kendo Grid에서 전달하는 Sorting 옵션을 전달하기 위한 파라미터 클래스입니다.")
public class KendoSort implements Serializable {

  private static final long serialVersionUID = -7317586590810302888L;

  @Schema(
    example = "asc",
    allowableValues = "asc/desc",
    required = true,
    description = "필수값. 이 값이 쿼리문 안에서 Order by를 할 때 오름차순/내림차순 중 어느 것으로 정렬할지를 결정합니다."
  )
  private String dir;

  @Schema(
    example = "reqNo",
    required = true,
    description = "필수값. 이 값은 어떤 컬럼을 기준으로 정렬할 것인지를 전달합니다. DB 컬럼명이 언더스코어 형식으로 되어 있더라도 카멜 케이스 형태로 전달하면 됩니다. (예: req_no => reqNo)"
  )
  private String field;

  public KendoSort() {
    super();
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  @Override
  public String toString() {
    return "KendoSort [dir=" + dir + ", field=" + field + "]";
  }

}
