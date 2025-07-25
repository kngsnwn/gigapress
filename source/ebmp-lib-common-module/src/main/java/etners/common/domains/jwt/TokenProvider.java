package etners.common.domains.jwt;

import common.util.cipher.AES256;
import common.util.string.StringUtil;
import etners.ebmp.lib.jpa.entity.epc.epecUserMst.EpcUserMstCommon;
import etners.ebmp.lib.jpa.repo.epcUserMst.EpcUserMstCommonRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

  private static final byte[] secretKey = TextCodec.BASE64.decode("ZXRuZXJzLWVibXAtMS4wdi1hcGktcHJvZA==");
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private final RedisService redisService;
  private final EpcUserMstCommonRepository epcUserMstCommonRepository;
  private final CommonUserDetailsService commonUserDetailsService;


  public String resolveAccessToken(HttpServletRequest httpServletRequest) {
    String bearerToken = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public Claims extractClaims(String token) {
    Jws<Claims> parsedToken = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

    return parsedToken.getBody();
  }

  public void validateTokenAndThrowException(String token, Claims claims) {
    String unqUserId = this.getUnqUserId(claims);
    String wmGbn = this.getWmGbn(claims);
    String solCd = this.getSolCd(claims);
    if (StringUtil.isNotEmpty(wmGbn) && StringUtil.isNotEmpty(solCd) && "01".equals(wmGbn)) {
      String accessToken = redisService.getHashOps("jwtToken:" + unqUserId + wmGbn + solCd, "accessToken");

      if (!accessToken.equals(token)) {
        throw new IllegalArgumentException();
      }
    }
  }

  public String getEmail(Claims claims) {
    return claims.getAudience();
  }

  public String getDeptCd(Claims claims) {
    return decodeClaimsData(claims, "dpt");
  }

  public String getSolCd(Claims claims) {
    return decodeClaimsData(claims, "solCd");
  }

  public String getSabun(Claims claims) {
    return decodeClaimsData(claims, "san");
  }

  public String getCmpyCd(Claims claims) {
    return decodeClaimsData(claims, "cce");
  }

  public String getUnqUserId(Claims claims) {
    return decodeClaimsData(claims, "uat");
  }

  public String getWmGbn(Claims claims) {
    return decodeClaimsData(claims, "wmGbn");
  }

  public String getWorkspaceCd(Claims claims) {
    return decodeClaimsData(claims, "wsc");
  }

  private String getSolCds(Claims claims) {
    return decodeClaimsData(claims, "scd");
  }

  public String decodeClaimsData(Claims claims, String claimsKey) {
    if (claims != null && claimsKey != null) {
      if (!claims.containsKey(claimsKey)) {
        return "";
      } else {
        String data = this.getClaimsValueOfString(claims, claimsKey);
        AES256 cipher = AES256.getInstance();
        return cipher.AES_Decode(data);
      }
    } else {
      throw new IllegalArgumentException("잘못된 값입니다.");
    }
  }

  public String getClaimsValueOfString(Claims claims, String claimsKey) {
    Object value = claims.get(claimsKey);
    String data = null;
    if (value instanceof String) {
      data = (String) value;
    } else {
      data = value.toString();
    }

    return data;
  }

  public void getAuthentication(String token, Claims claims) {
    String unqUserId = getUnqUserId(claims);
    String cmpyCd = getCmpyCd(claims);
    String solCd = getSolCd(claims);
    String wmGbn = getWmGbn(claims);
    String email = getEmail(claims);
    String sabun = getSabun(claims);
    String deptCd = getDeptCd(claims);
    String workspaceCd = getWorkspaceCd(claims);
    String solCds = getSolCds(claims);

    if (StringUtil.isNotEmpty(cmpyCd)) {
      String roleName = "00001".equals(cmpyCd) ? "ETNERS" : "CLIENT";
      switch (roleName) {
        case "ETNERS", "CLIENT" -> {
          EpcUserMstCommon epcUserMstCommon = epcUserMstCommonRepository.findByUnqUserId(unqUserId);
          if (ObjectUtils.isNotEmpty(epcUserMstCommon)) {
            saveAuthentication(epcUserMstCommon, roleName, solCd, wmGbn, workspaceCd, token, email, sabun, deptCd, solCds);
          } else {
            log.error("사용자 정보를 찾을 수 없습니다.");
          }
        }
        default -> log.info("잘못된 토큰입니다.");
      }
    } else {
      log.info("토큰에서 CMPY_CD 추출 실패하였습니다.");
    }

  }

  public void saveAuthentication(EpcUserMstCommon epcUserMstCommon, String roleName, String solCd, String wmGbn, String workspaceCd, String token, String email, String sabun, String deptCd, String solCds) {
    CommonUserDetails userDetails = (CommonUserDetails) commonUserDetailsService.loadUserByUsername(epcUserMstCommon, roleName, solCd, wmGbn, workspaceCd, token, email, sabun, deptCd, solCds);

    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
