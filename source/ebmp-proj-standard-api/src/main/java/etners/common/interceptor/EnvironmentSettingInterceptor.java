package etners.common.interceptor;

import etners.common.domains.jwt.CommonUserData;
import etners.common.domains.jwt.CurrentUserDataLoader;
import etners.common.domains.jwt.TokenProvider;
import etners.common.util.enumType.AuthType;
import etners.common.util.enumType.SolutionType;
import etners.common.util.enumType.WebAccessType;
import etners.common.util.scope.CurrentUserData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class EnvironmentSettingInterceptor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentSettingInterceptor.class);

  private final CurrentUserData currentUserData;
  private final TokenProvider tokenProvider;
  private final CurrentUserDataLoader currentUserDataLoader;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    String accessToken = tokenProvider.resolveAccessToken(request);

    CommonUserData commonUserData = currentUserDataLoader.getCommonUserData();
    String unqUserId = commonUserData.getUnqUserId();
    String authCd = commonUserData.getAuthCd();
    String workspaceCd = commonUserData.getWorkspaceCd();
    String wmGbn = commonUserData.getWmGbn();
    WebAccessType webAccessType = WebAccessType.get(wmGbn);
    AuthType authType = AuthType.get(authCd);

    currentUserData.setUnqUserId(unqUserId);
    currentUserData.setCmpyCd(commonUserData.getCmpyCd());
    currentUserData.setWorkspaceCd(workspaceCd);
    currentUserData.setSolCd(SolutionType.ESRM.getCode());
    currentUserData.setAuthCd(authCd);
    currentUserData.setAccessToken(accessToken);
    currentUserData.setWebAccessType(webAccessType);
    currentUserData.setEtners(currentUserData.isEtners());

    // isCm 계산 - 이트너스 직원이 아니면서 CM 권한을 가진 경우
    boolean isCm = !commonUserData.getIsEtners() && AuthType.CM.equals(authType);
    currentUserData.setCm(isCm);

    return true;

  }

}
