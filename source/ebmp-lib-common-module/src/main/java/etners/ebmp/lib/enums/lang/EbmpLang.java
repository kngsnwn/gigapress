package etners.ebmp.lib.enums.lang;

import java.util.Locale;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : EbmpLang.java
 * @Description : 기존에 사용되던 다국어 요청 코드를 상수화한 클래스. 기존에 사용하던 인터페이스를 그대로 유지하기 위해 enum 대신 클래스로 설계하였다.
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 3. 23.  oxide     최초생성
 * @see
 * @since 2019. 3. 23.
 */
public enum EbmpLang {
  KO(Locale.KOREA.getLanguage(), "한국어"),
  EN(Locale.ENGLISH.getLanguage(), "영어"),
  ZH(Locale.CHINA.getLanguage(), "중국어"),
  VN("vn", "베트남어"),
  JP(Locale.JAPAN.getLanguage(), "일본어");


  private final String locale;

  private final String description;

  EbmpLang(String locale, String description) {
    this.locale = locale;
    this.description = description;
  }

  public String getLocale() {
    return locale;
  }

  public String getDescription() {
    return description;
  }

  public static EbmpLang get(String locale) {
    for (EbmpLang lang : values()) {
      if (lang.getLocale().equals(locale)) {
        return lang;
      }
    }

    return null;
  }

}
