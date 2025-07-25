package common.lang;

import java.util.Locale;

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
