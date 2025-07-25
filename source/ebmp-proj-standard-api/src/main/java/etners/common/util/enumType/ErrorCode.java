package etners.common.util.enumType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  DEFAULT("4000", "요청한 데이터가 없거나 조회할 수 있는 권한이 없습니다."),
  NOT_FOUND_AUTH("4253", "요청한 데이터가 없거나 조회할 수 있는 권한이 없습니다."),
  ;

  private final String code;
  private final String desc;

}
