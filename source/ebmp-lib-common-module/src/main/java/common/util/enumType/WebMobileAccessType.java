package common.util.enumType;

public enum WebMobileAccessType {
  WEB("01", "웹(PC)"), MOBILE_ALL("02", "모바일 웹/네이티브 모두"), MOBILE_IOS("02", "모바일 iOS"), MOBILE_ANDROID("02", "모바일 안드로이드"), MOBILE_WEB("02", "모바일 웹");

  private final String wbGbn;

  private final String description;

  WebMobileAccessType(String wbGbn, String description) {
    this.wbGbn = wbGbn;
    this.description = description;
  }

  public String getWbGbn() {
    return wbGbn;
  }

  public String getDescription() {
    return description;
  }
}
