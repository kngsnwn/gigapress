package etners.common.domains.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonUserData {

  private String accessToken;
  private String email;
  private String sabun;
  private String deptCd;
  private String unqUserId;
  private String workspaceCd;
  private String wmGbn;
  private String cmpyCd;
  private String solCd;
  private String authCd;
  private Boolean isEtners;
  private String solCds;
}
