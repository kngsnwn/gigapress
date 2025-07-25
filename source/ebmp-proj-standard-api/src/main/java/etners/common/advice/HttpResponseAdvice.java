package etners.common.advice;

import common.util.string.StringUtil;
import etners.common.config.exception.AccessCheckException;
import etners.common.config.exception.ApplicationException;
import etners.common.util.scope.CurrentUserData;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.factory.ResultModelFactoryG2;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.ebmp.lib.enums.lang.EbmpLang;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RequiredArgsConstructor
@ControllerAdvice
@RestControllerAdvice
public class HttpResponseAdvice implements ResponseBodyAdvice<Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseAdvice.class);

  private final CurrentUserData currentUserData;


  @Override
  public boolean supports(

    MethodParameter returnType,
    Class<? extends HttpMessageConverter<?>> converterType
  ) {

    return true;
  }

  @Override
  public Object beforeBodyWrite(
    Object body,
    MethodParameter returnType,
    MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType,
    ServerHttpRequest request,
    ServerHttpResponse response
  ) {
    return body;
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResultModel<Object> handleValidationExceptions(Exception e) {
    EbmpLang ebmpLang = currentUserData.getEbmpLang();
    int msgCode = 9999;

    BindingResult bindingResult;
    if (e instanceof MethodArgumentNotValidException) {
      bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
    } else {
      bindingResult = ((BindException) e).getBindingResult();
    }

    if (bindingResult.hasErrors()) {
      List<ObjectError> objectErrorList = bindingResult.getAllErrors();
      if (ObjectUtils.isNotEmpty(objectErrorList)) {
        ObjectError objectError = objectErrorList.get(0);
        String errorCode = objectError.getCode();
        if (("NotBlank".equals(errorCode) || "NotEmpty".equals(errorCode) || "NotNull".equals(errorCode) || "ValidListSize".equals(errorCode))) {
          msgCode = 5025;
          if (StringUtil.isNotEmpty(objectError.getDefaultMessage())) {
            String message = objectError.getDefaultMessage();
            return ResultStatusManager.failResultModelWithCustomMessage(ebmpLang, message, null);
          }
        } else {
          String message = objectError.getDefaultMessage();
          return ResultStatusManager.failResultModelWithCustomMessage(ebmpLang, message, null);
        }
      }
    }

    return ResultStatusManager.failResultModel(ebmpLang, msgCode);
  }

  @ExceptionHandler({SignatureException.class, MalformedJwtException.class, UnsupportedJwtException.class, ExpiredJwtException.class})
  public ResultModel<Object> exception(Exception e) {
    return ResultStatusManager.failResultModel(currentUserData.getEbmpLang(), 4202);
  }

  @ExceptionHandler({AccessCheckException.class})
  public ResultModel<Object> exception(AccessCheckException e) {
    return ResultStatusManager.failResultModel(currentUserData.getEbmpLang(), "4009"); // 요청받은 솔루션에 대한 권한 값이 존재하지 않습니다. 관리자에게 문의해주세요.
  }

  @InitBinder
  public void InitBinder(WebDataBinder dataBinder) {
    StringTrimmerEditor ste = new StringTrimmerEditor(true);    // emptyAsNull: true (빈문자열은 null로 파싱함)
    dataBinder.registerCustomEditor(String.class, ste);
  }

  @ExceptionHandler({ApplicationException.class})
  public ResultModel<Object> exception(ApplicationException e) {
    return ResultStatusManager.failResultModel(currentUserData.getEbmpLang(), e.getErrorCode());
  }

  @ResponseBody
  @ExceptionHandler(Exception.class)
  public ResultModel<Boolean> errorHandler(Exception e, HttpServletRequest req) {
    return ResultModelFactoryG2.getInstance().failResultModel(currentUserData.getEbmpLang());
  }
}
