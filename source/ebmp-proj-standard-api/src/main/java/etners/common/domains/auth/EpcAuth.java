package etners.common.domains.auth;


import common.auth.AuthCdType;

public enum EpcAuth implements AuthCdType<EpcAuth> {
  ETNERS_MANAGER("1019", "10", "이트너스담당자"),   //EM
  ETNERS_LEADER("1019", "15", "이트너스관리자"),    //EL
  CLIENT_EMPOLYEE("1019", "20", "클라이언트임직원"),
  CLIENT_MANAGER("1019", "25", "클라이언트담당자"),
  SYSTEM_ADMINISTRATOR("1019", "99999", "시스템관리자"),
  ;

  private String solCd;

  private String authCd;

  private String description;

  EpcAuth(String solCd, String authCd, String description) {
    this.solCd = solCd;
    this.authCd = authCd;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String getSolCd() {
    return solCd;
  }

  public boolean isMatchAuthCd(String authCd) {
    return this.authCd.equals(authCd);
  }

  @Override
  public String getAuthCd() {
    return authCd;
  }

  public static EpcAuth get(String authCd) {
    for (EpcAuth auth : values()) {
      if (auth.getAuthCd().equals(authCd)) {
        return auth;
      }
    }

    return null;
  }

}
