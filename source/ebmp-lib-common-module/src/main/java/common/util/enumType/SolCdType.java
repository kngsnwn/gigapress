package common.util.enumType;

public enum SolCdType {
  CBMP_PLATFORM("0000", "경영지원플랫폼"), CBMP_PLATFORM_ADMIN("0001", "경영지원플랫폼 관리자"), ESOP_PORTAL("0002", "ESOP Portal"), UTOPIA("0100", "경영지원플랫폼"), CONCIERGE("1000", "컨시어지 배차관리"), TNA("1001", "경영지원플랫폼"), MCP("1002", "경영지원플랫폼"), UNSPECIFIED_NAME1("1003", "급여관리(현재 없는 솔루션)"), UNSPECIFIED_NAME2("1004", "연말정산(현재 없는 솔루션)"), UNSPECIFIED_NAME3("1005", "사내물류(현재 없는 솔루션)"), UNSPECIFIED_NAME4("1006",
    "GHD(현재 없는 솔루션)"), GEW("1007", "경영지원플랫폼"), UNSPECIFIED_NAME5("1008", "회의실관리(현재 없는 솔루션)"), MVI("1009", "[SOP]입주계약서비스"), CEG("1010", "[SOP]출입관리서비스"), OPM("1011", "[SOP]운영관리(웹)"), SCHEDULE_MEETING("1012", "[SOP]회의실예약(웹앱)"), HELP_DESK("1013", "[SOP]Help Desk(사용자 모바일)"), COFFEE("1014", "[SOP]Coffee(사용자 모바일)"), MOBILE_PASS("1015", "[SOP]모바일 출입증(사용자 모바일)"), MOBILE_TOILET("1016",
    "[SOP]화장실"), LEGACY_ETNERS_SHOP("2000", "이트너스샵"), LEGACY_ETNERS_HOUSING_MALL("2001", "이트너스몰"), LEGACY_ETNERS_HOUSING_SHOP("2002", "이트너스 하우징샵"), LEGACY_ETNERS_BIDDING("2003", "이트너스비딩"), LEGACY_ETNERS_MOVING("2004", "이트너스무빙"), LEGACY_CHONGMU("2005", "총무닷컴"), LEGACY_ETNERS_MALL("2006", "이트너스DMS"), LEGACY_PAYROLL("2007", "이트너스페이롤"), LEGACY_ONLINE_SALARY_AGREEMENT("2008", "온라인 연봉 계약서");

  private final String solCd;

  private final String description;

  SolCdType(String solCd, String description) {
    this.solCd = solCd;
    this.description = description;
  }

  public String getSolCd() {
    return solCd;
  }

  public String getDescription() {
    return description;
  }

  public static SolCdType get(String currentSolCd) {
    for (SolCdType solCdType : values()) {
      if (solCdType.getSolCd().equals(currentSolCd)) {
        return solCdType;
      }
    }

    return null;
  }

}
