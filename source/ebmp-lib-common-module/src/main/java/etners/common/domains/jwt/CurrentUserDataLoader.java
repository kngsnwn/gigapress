package etners.common.domains.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserDataLoader {

  public CommonUserData getCommonUserData() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof CommonUserDetails) {
      return ((CommonUserDetails) authentication.getPrincipal()).getCommonUserData();
    }
    return null;
  }
}
