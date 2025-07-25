package etners.common.interceptor;

import etners.common.util.enumType.WebAccessType;
import etners.common.util.scope.CurrentUserData;
import etners.ebmp.lib.enums.lang.EbmpLang;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class CommonInterceptor implements HandlerInterceptor {

  private final CurrentUserData currentUserData;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String locale = request.getHeader("locale");
    EbmpLang cbmpLang = ObjectUtils.isEmpty(locale) || EbmpLang.get(locale) == null ? EbmpLang.KO : EbmpLang.get(locale);

    currentUserData.setWebAccessType(WebAccessType.WEB);
    currentUserData.setEbmpLang(cbmpLang);

    return true;
  }
}
