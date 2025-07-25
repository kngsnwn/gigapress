package etners.common.util.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import etners.common.domains.jwt.TokenProvider;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.factory.ResultModelFactoryG2;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.ebmp.lib.enums.lang.EbmpLang;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;


@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends GenericFilterBean {

  private final TokenProvider tokenProvider;
  private final ObjectMapper objectMapper;

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String token = tokenProvider.resolveAccessToken(httpServletRequest);
    String requestURI = httpServletRequest.getRequestURI();

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    if (StringUtils.hasText(token)) {
      validateTokenByClaims(token, request, response, filterChain, requestURI);
    } else {
      returnError(request, response, filterChain, requestURI, "4202", HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private void returnError(ServletRequest request, ServletResponse response, FilterChain filterChain, String requestURI, String errorCode, int httpStatus) throws IOException {
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    httpServletResponse.setStatus(httpStatus);

    ResultModel<Object> responseStatusException = ResultModelFactoryG2.getInstance().makeResultModel(EbmpLang.KO, ResultStatusManager.getResultStatus(EbmpLang.KO, errorCode));
    objectMapper.writeValue(httpServletResponse.getWriter(), responseStatusException);
  }

  private void validateTokenByClaims(String token, ServletRequest request, ServletResponse response, FilterChain filterChain, String requestURI) throws ServletException, IOException {
    try {
      Claims claims = tokenProvider.extractClaims(token);
      tokenProvider.validateTokenAndThrowException(token, claims);
      tokenProvider.getAuthentication(token, claims);
      filterChain.doFilter(request, response);
    } catch (MalformedJwtException | SignatureException | SecurityException var8) {
      log.info("잘못된 JWT 서명입니다.");
      this.returnError(request, response, filterChain, requestURI, "4202", 403);
    } catch (ExpiredJwtException var9) {
      log.info("만료된 JWT 토큰입니다.");
      this.returnError(request, response, filterChain, requestURI, "4204", 401);
    } catch (UnsupportedJwtException var10) {
      log.info("지원되지 않는 JWT 토큰입니다.");
      this.returnError(request, response, filterChain, requestURI, "4202", 403);
    } catch (IllegalArgumentException var11) {
      log.info("JWT 토큰이 잘못되었습니다.");
      this.returnError(request, response, filterChain, requestURI, "4202", 403);
    }
  }
}
