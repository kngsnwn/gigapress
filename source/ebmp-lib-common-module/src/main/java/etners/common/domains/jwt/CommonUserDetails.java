package etners.common.domains.jwt;


import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CommonUserDetails extends User {

  private final CommonUserData commonUserData;

  public CommonUserDetails(String userId, String userPwd, CommonUserData commonUserData, Collection<? extends GrantedAuthority> authorities) {
    super(userId, userPwd, authorities);
    this.commonUserData = commonUserData;
  }
}
