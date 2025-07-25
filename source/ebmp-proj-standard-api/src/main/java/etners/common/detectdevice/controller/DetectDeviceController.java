package etners.common.detectdevice.controller;

import common.util.string.StringUtil;
import etners.common.detectdevice.domain.DeviceConnectionInfo;
import etners.common.util.session.SessionBinder;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DetectDeviceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectDeviceController.class);


  /**
   * <pre>
   * 메소드명 : updateDetectDeviceType
   * 작성자  : oxide
   * 작성일  : 2018. 10. 22.
   * 설명   :
   * </pre>
   *
   * @param req
   * @param res
   * @param session
   * @return
   * @throws Exception
   */
  @Operation(summary = "접속 하는 기기 또는 브라우저의 정보르 가져 오는 API")
  @RequestMapping(value = "/web/intro/detect/updateDetectDeviceType", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String updateDetectDeviceType(HttpServletRequest req, HttpServletResponse res,
      HttpSession session, @RequestBody DeviceConnectionInfo deviceConnectionInfo)
      throws Exception {
    LOGGER.debug("deviceConnectionInfo : " + deviceConnectionInfo);

    SessionBinder.updateDeviceConnectionInfo(session, deviceConnectionInfo);

    return StringUtil.resultMessageToJson(true);
  }
}
