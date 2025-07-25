package etners.common.util.session;

import etners.common.detectdevice.domain.DeviceConnectionInfo;
import java.security.PrivateKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionBinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionBinder.class);

  private final static String USER_SESSION_KEY = "userSession";
  private final static String PRODUCTION_MODE_KEY = "productionMode";
  private final static String DEVICE_CONNECTION_INFO_KEY = "deviceConnectionInfo";
  private final static String RSA_KEY = "__RSA_Key__";

  /**
   * <pre>
   * 메소드명	: isNotExistLoginUserSession
   * 작성자	: oxide
   * 작성일	: 2018. 7. 24.
   * 설명	:
   * </pre>
   *
   * @param session
   * @return
   */
  public static boolean isNotExistLoginUserSession(HttpSession session) {
    return session.getAttribute(USER_SESSION_KEY) == null;
  }

  public static boolean isExistLoginUserSession(HttpSession session) {
    return !isNotExistLoginUserSession(session);
  }

  public static boolean isDeviceVerification(HttpSession session) {
    DeviceConnectionInfo deviceConnectionInfo = (DeviceConnectionInfo) session
        .getAttribute(DEVICE_CONNECTION_INFO_KEY);

    return deviceConnectionInfo != null;
  }

  public static DeviceConnectionInfo bindDeviceConnectionInfo(HttpServletRequest request,
      HttpSession session) {
    String userAgent = request.getParameter("userAgent");
    String mobile = request.getParameter("mobile");
    String tablet = request.getParameter("tablet");
    String phone = request.getParameter("phone");
    String os = request.getParameter("os");
    String isIPhone = request.getParameter("isIPhone");
    String isBot = request.getParameter("isBot");
    String webkit = request.getParameter("webkit");
    String build = request.getParameter("build");

    if (LOGGER.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();

      sb.append("\n\n>>>>>>>>>>> Detect Device\n");
      sb.append("mobile : " + mobile + "\n");
      sb.append("phone : " + phone + "\n");
      sb.append("tablet : " + tablet + "\n");
      sb.append("userAgent : " + userAgent + "\n");
      sb.append("os : " + os + "\n");
      sb.append("isIPhone : " + isIPhone + "\n");
      sb.append("isBot : " + isBot + "\n");
      sb.append("webkit : " + webkit + "\n");
      sb.append("build : " + build + "\n\n");

      LOGGER.debug(sb.toString());
    }

    String connectDeviceType = null;

    if (mobile == null && tablet == null) {
      connectDeviceType = "PC";
    } else {
      connectDeviceType = "MOBILE";
    }

    DeviceConnectionInfo deviceConnectionInfo = new DeviceConnectionInfo(connectDeviceType,
        userAgent, mobile, tablet, phone, os, isIPhone, isBot, webkit, build);

    settingDeviceConnectionInfo(session, deviceConnectionInfo);

    return deviceConnectionInfo;
  }

  private static void settingDeviceConnectionInfo(HttpSession session,
      DeviceConnectionInfo deviceConnectionInfo) {
    session.setAttribute(DEVICE_CONNECTION_INFO_KEY, deviceConnectionInfo);
  }

  /**
   * <pre>
   * 메소드명	: getConnectDeviceType
   * 작성자	: oxide
   * 작성일	: 2018. 10. 22.
   * 설명	:
   * </pre>
   *
   * @param session
   * @return
   */
  public static DeviceConnectionInfo getConnectDeviceType(HttpSession session) {
    DeviceConnectionInfo deviceConnectionInfo = (DeviceConnectionInfo) session
        .getAttribute(DEVICE_CONNECTION_INFO_KEY);

    return deviceConnectionInfo;
  }

  /**
   * <pre>
   * 메소드명	: getConnectDeviceType
   * 작성자	: oxide
   * 작성일	: 2018. 11. 6.
   * 설명	:
   * </pre>
   *
   * @param request
   */
  public static DeviceConnectionInfo getConnectDeviceType(HttpServletRequest request) {
    HttpSession session = request.getSession();

    return getConnectDeviceType(session);
  }

  /**
   * <pre>
   * 메소드명	: updateDeviceConnectionInfo
   * 작성자	: oxide
   * 작성일	: 2018. 10. 22.
   * 설명	:
   * </pre>
   *
   * @param session
   * @param deviceConnectionInfo
   */
  public static void updateDeviceConnectionInfo(HttpSession session,
      DeviceConnectionInfo deviceConnectionInfo) {
    String deviceConnectionType = null;

    if (deviceConnectionInfo.getMobile() == null && deviceConnectionInfo.getTablet() == null) {
      deviceConnectionType = "PC";
    } else {
      deviceConnectionType = "MOBILE";
    }

    deviceConnectionInfo.setConnectDeviceType(deviceConnectionType);

    settingDeviceConnectionInfo(session, deviceConnectionInfo);
  }

  /**
   * <pre>
   * 메소드명	: isMobileUser
   * 작성자	: oxide
   * 작성일	: 2018. 11. 6.
   * 설명	:
   * </pre>
   *
   * @param session
   * @return
   */
  public static boolean isMobileUser(HttpSession session) {
    DeviceConnectionInfo deviceConnectionInfo = getConnectDeviceType(session);

    //접속 기기가 파악되지 않은 상태라면 무조건 Desktop으로 본다.
    if (deviceConnectionInfo == null) {
      return false;
    }

    return deviceConnectionInfo.isMobile();
  }

  /**
   * <pre>
   * 메소드명	: isMobileUser
   * 작성자	: oxide
   * 작성일	: 2018. 11. 6.
   * 설명	:
   * </pre>
   *
   * @param req
   * @return
   */
  public static boolean isMobileUser(HttpServletRequest req) {
    HttpSession session = req.getSession();

    return isMobileUser(session);
  }

  /**
   * <pre>
   * 메소드명	: isDesktopUser
   * 작성자	: oxide
   * 작성일	: 2018. 11. 6.
   * 설명	:
   * </pre>
   *
   * @param session
   * @return
   */
  public static boolean isDesktopUser(HttpSession session) {
    return !isMobileUser(session);
  }

  /**
   * <pre>
   * 메소드명	: isDesktopUser
   * 작성자	: oxide
   * 작성일	: 2018. 11. 6.
   * 설명	:
   * </pre>
   *
   * @param req
   * @return
   */
  public static boolean isDesktopUser(HttpServletRequest req) {
    HttpSession session = req.getSession();

    return !isMobileUser(session);
  }

  public static void bindRsaPrivateKey(HttpSession session, PrivateKey privateKey) {
    session.setAttribute(RSA_KEY, privateKey);
  }

  public static void bindProductionMode(HttpSession session, String ynFlag) {
    session.setAttribute(PRODUCTION_MODE_KEY, ynFlag);
  }

  public static PrivateKey getRsaPrivateKey(HttpSession session) {
    PrivateKey privateKey = (PrivateKey) session.getAttribute(RSA_KEY);

    return privateKey;
  }

}
