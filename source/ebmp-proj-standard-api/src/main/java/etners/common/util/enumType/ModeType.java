package etners.common.util.enumType;

import etners.common.util.converter.CommonType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModeType implements CommonType {

  INSERT("01", "저장", "신규"),
  UPDATE("02", "수정", "변경"),
  DELETE("03", "삭제", "취소");

  private String code;

  private String desc;

  private String keyword;
}
