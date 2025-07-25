package etners.common.domains.jwt;

import etners.ebmp.lib.jpa.entity.epc.epcUserSol.EpcUserSolCommon;
import etners.ebmp.lib.jpa.entity.epc.epecUserMst.EpcUserMstCommon;
import etners.ebmp.lib.jpa.repo.epcUserMst.EpcUserMstCommonRepository;
import etners.ebmp.lib.jpa.repo.epcUserSol.EpcUserSolCommonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonUserDetailsService implements UserDetailsService {

  private final EpcUserMstCommonRepository epcUserMstCommonRepository;
  private final EpcUserSolCommonRepository epcUserSolCommonRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    EpcUserMstCommon epcUserMstCommon = epcUserMstCommonRepository.findByUserId(username);
    String cmpyCd = epcUserMstCommon.getCmpyCd();
    String role = "00001".equals(cmpyCd) ? "ETNERS" : "CLIENT";
    CommonUserData commonUserData = CommonUserData.builder()
      .unqUserId(epcUserMstCommon.getUnqUserId())
      .cmpyCd(cmpyCd)
      .build();
    List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority(role));
    return new CommonUserDetails(epcUserMstCommon.getUserId(), epcUserMstCommon.getUserPwd(), commonUserData, grantedAuthorities);
  }

  public UserDetails loadUserByUsername(EpcUserMstCommon epcUserMstCommon, String role, String solCd, String wmGbn, String workspaceCd, String token, String email, String sabun, String deptCd, String solCds) throws UsernameNotFoundException {
    EpcUserSolCommon epcUserSolCommon = epcUserSolCommonRepository.findByUnqUserIdAndSolCdAndWmGbnAndUseYn(epcUserMstCommon.getUnqUserId(), solCd, wmGbn, true);
    if (ObjectUtils.isEmpty(epcUserSolCommon)) {
      log.info("요청받은 솔루션에 대한 권한 값이 존재하지 않습니다.");
      throw new UsernameNotFoundException("The user has no authorization to the solution.");
    }
    CommonUserData commonUserData = CommonUserData.builder()
      .accessToken(token)
      .email(email)
      .sabun(sabun)
      .deptCd(deptCd)
      .unqUserId(epcUserMstCommon.getUnqUserId())
      .solCd(solCd)
      .authCd(epcUserSolCommon.getAuthCd())
      .wmGbn(wmGbn)
      .cmpyCd(epcUserMstCommon.getCmpyCd())
      .workspaceCd(workspaceCd)
      .isEtners("00001".equals(epcUserMstCommon.getCmpyCd()))
      .solCds(solCds)
      .build();

    List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority(role));
    return new CommonUserDetails(epcUserMstCommon.getUserId(), epcUserMstCommon.getUserPwd(), commonUserData, grantedAuthorities);
  }
}
