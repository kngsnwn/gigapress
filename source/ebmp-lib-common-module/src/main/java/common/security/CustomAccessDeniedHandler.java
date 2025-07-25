package common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.factory.ResultModelFactoryG2;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.ebmp.lib.enums.lang.EbmpLang;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
    ResultModel<Object> responseStatusException = ResultModelFactoryG2.getInstance().makeResultModel(EbmpLang.KO, ResultStatusManager.getResultStatus(EbmpLang.KO, "4202"));
    String jsonResponse = objectMapper.writeValueAsString(responseStatusException);

    LOGGER.error("접근 권한이 없습니다. :: CustomAccessDeniedHandler.class");

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write(jsonResponse);
  }
}

