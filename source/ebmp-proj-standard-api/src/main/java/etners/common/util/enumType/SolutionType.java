package etners.common.util.enumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import etners.common.util.converter.CommonType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SolutionType implements CommonType {

  CONTRACT_SEAT("1009", "입주계약좌석 서비스"),
  CONFERENCE("1012", "회의실예약"),
  ESRM("1019", "ESRM"),
  DS_MOVE("1022", "DS MOVE");

  private String code;

  private String desc;

  public static SolutionType get(String s) {
    for (SolutionType t : SolutionType.values()) {
      if (t.getCode().equals(s)) {
        return t;
      }
    }
    return null;
  }


}
