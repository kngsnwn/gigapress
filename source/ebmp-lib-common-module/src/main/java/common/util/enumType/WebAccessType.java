package common.util.enumType;

import common.util.convert.CommonType;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebAccessType implements CommonType {

  WEB("01", "웹"),
  MOBILE("02", "모바일"),
  ADMIN_WEB("11", "관리자 웹"),
  ADMIN_MOBILE("12", "관리자 모바일");

  private String code;

  private String desc;

  public static WebAccessType get(String s) {
    return Arrays.stream(WebAccessType.values()).filter(w -> s.equals(w.getCode())).findFirst().orElse(WEB);
  }

  public static WebAccessType getAccessTypeByUserAgent(String userAgent) {
    return WebAccessType.WEB;
  }

  public boolean isMobileType() {
    return MOBILE.equals(this) || ADMIN_MOBILE.equals(this);
  }
}
