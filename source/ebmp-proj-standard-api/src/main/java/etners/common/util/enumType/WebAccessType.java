package etners.common.util.enumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import etners.common.util.converter.CommonType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebAccessType implements CommonType {

  WEB("01", "웹"),
  MOBILE("02", "모바일"),
  ADMIN_WEB("11", "관리자 웹"),
  ADMIN_MOBILE("12", "관리자 모바일"),
  DWMS_WEB("91", "DWMS(DS ON) 웹"),
  DWMS_MOBILE("92", "DWMS(DS ON) 모바일");

  private String code;

  private String desc;

  public static WebAccessType get(String s) {
    for (WebAccessType t : WebAccessType.values()) {
      if (t.getCode().equals(s)) {
        return t;
      }
    }
    return WEB;
  }

  public boolean isMobile() {
    return this.equals(MOBILE) || this.equals(ADMIN_MOBILE) || this.equals(DWMS_MOBILE);
  }
}
