package etners.common.util.scope;

import etners.common.util.enumType.WebAccessType;
import etners.ebmp.lib.enums.lang.EbmpLang;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Getter
@Setter
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserData {

  private EbmpLang ebmpLang;
  private WebAccessType webAccessType;
  private String unqUserId;
  private String cmpyCd;
  private String accessToken;
  private String workspaceCd;
  private String solCd;
  private String authCd;
  private boolean isEtners;
  private boolean isCm;
}
