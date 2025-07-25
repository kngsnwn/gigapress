package common.util.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.factory.ResultModelFactoryG2;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.ebmp.lib.enums.lang.EbmpLang;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RequiredArgsConstructor
public class NotFoundUrlFilter extends OncePerRequestFilter {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String requestUrl = request.getRequestURI();

    ObjectMapper mapper = new ObjectMapper();
    if (!urlExists(requestUrl)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      ResultModel<Object> responseStatusException = ResultModelFactoryG2.getInstance().makeResultModel(EbmpLang.KO, ResultStatusManager.getResultStatus(EbmpLang.KO, 4000));
      mapper.writeValue(response.getWriter(), responseStatusException);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean urlExists(String url) {
    Set<String> staticPatterns = removeDynamicPatternsFromMappings();
    return staticPatterns.stream().anyMatch(pattern -> matchPattern(pattern, url));
  }

  private Set<String> removeDynamicPatternsFromMappings() {
    Set<String> staticPatterns = new HashSet<>();
    Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
    for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
      RequestMappingInfo mappingInfo = entry.getKey();
      Set<String> patterns = mappingInfo.getPatternValues();
      patterns.forEach(pattern -> {
        if (!isDynamicPattern(pattern)) {
          staticPatterns.add(pattern);
        } else {
          staticPatterns.add(removeDynamicPatternVariables(pattern));
        }
      });
    }
    return staticPatterns;
  }

  private boolean matchPattern(String pattern, String url) {
    pattern = pattern.replace("*", ".*").replace("?", ".");
    return url.matches(pattern);
  }

  private boolean isDynamicPattern(String pattern) {
    return pattern.contains("{") && pattern.contains("}");
  }

  private String removeDynamicPatternVariables(String pattern) {
    return pattern.replaceAll("\\{[^/]+\\}", "*");
  }
}
