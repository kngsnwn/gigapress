package common.util.network;

import common.util.string.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtil.class);

  public static String getClientIpAddress(HttpServletRequest request) {
    String clientIpAddress = null;

    clientIpAddress = request.getHeader("X-Forwarded-For");

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("Proxy-Client-IP");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("WL-Proxy-Client-IP");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("HTTP_CLIENT_IP");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("X-Real-IP");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("X-RealIP");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getHeader("REMOTE_ADDR");
    }

    if (isNotExistIpAddress(clientIpAddress)) {
      clientIpAddress = request.getRemoteAddr();
    }

    return clientIpAddress;
  }

  public static String getBrowserInfo(HttpServletRequest request) {
    String browser = "";
    try {
      String browserInfo = request.getHeader("User-Agent"); // 사용자 User-Agent 값 얻기

      if (browserInfo != null) {
        if (browserInfo.indexOf("Trident") > -1) {
          browser = "MSIE";
        } else if (browserInfo.indexOf("Chrome") > -1) {
          browser = "Chrome";
        } else if (browserInfo.indexOf("Opera") > -1) {
          browser = "Opera";
        } else if (browserInfo.indexOf("iPhone") > -1
          && browserInfo.indexOf("Mobile") > -1) {
          browser = "iPhone";
        } else if (browserInfo.indexOf("Android") > -1
          && browserInfo.indexOf("Mobile") > -1) {
          browser = "Android";
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return browser;
  }

  /**
   * <pre>
   * 메소드명	: isNotExistIpAddress
   * 작성자	: oxide
   * 작성일	: 2019. 7. 15.
   * 설명	: 아이피 정보가 존재하지 않거나 unknown으로 값이 들어오면 false를 리턴한다.
   * </pre>
   *
   * @param ip
   * @return
   */
  private static boolean isNotExistIpAddress(String ip) {
    return ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
  }

  /**
   * <pre>
   * 메소드명	: getServerDomain
   * 작성자	: chode8703
   * 작성일	: 2019. 8. 27.
   * 설명	: 서버의 도메인을 리턴한다.
   * </pre>
   *
   * @param request
   * @return
   */
  public static String getServerDomain(HttpServletRequest request) {
    String serverDomain = null;

    String domain = request.getScheme() + "://" + request.getServerName();
    int port = request.getServerPort();

    serverDomain = domain + ":" + port;

    return serverDomain;
  }

  public static JSONObject requestApplicationJsonReturnResultModel(String connectionUrl,
    HashMap<String, Object> parameterMap) {
    BufferedReader br = null;
    JSONObject responseJsonString = null;

    try {
      StringBuilder responseData = new StringBuilder();

      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }
        }
      };
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

      HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
          return true; // 모든 서버를 신뢰한다
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hv);

      URL url = new URL(connectionUrl);

      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json; utf-8");
      con.setRequestProperty("Accept", "application/json");

      con.setDoOutput(true);

      OutputStream os = con.getOutputStream();

      String jsonInputString = StringUtil.convertMapToJson(parameterMap);

      byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);

      os.write(input, 0, input.length);

      br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

      String responseLine = null;

      while ((responseLine = br.readLine()) != null) {
        responseData.append(responseLine.trim());
      }
      JSONParser jsonParser = new JSONParser();
      responseJsonString = (JSONObject) jsonParser.parse(responseData.toString());

    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          LOGGER.error(StringUtil.extractStackTrace(e));
        }
      }
    }

    return responseJsonString;
  }


  /**
   * <pre>
   * 메소드명 : requestApplicationJson
   * 작성자  : oxide
   * 작성일  : 2019. 8. 2.
   * 설명   :
   * </pre>
   *
   * @param connectionUrl
   * @param parameterMap
   * @return
   */
  public static String requestApplicationJson(String connectionUrl,
    HashMap<String, String> parameterMap) {
    BufferedReader br = null;
    String responseJsonString = null;

    try {
      StringBuilder responseData = new StringBuilder();

      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }
        }
      };
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

      HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
          return true; // 모든 서버를 신뢰한다
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hv);

      URL url = new URL(connectionUrl);

      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json; utf-8");
      con.setRequestProperty("Accept", "application/json");

      con.setDoOutput(true);

      OutputStream os = con.getOutputStream();

      String jsonInputString = StringUtil.convertMapToJson(parameterMap);

      byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);

      os.write(input, 0, input.length);

      br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

      String responseLine = null;

      while ((responseLine = br.readLine()) != null) {
        responseData.append(responseLine.trim());
      }

      responseJsonString = responseData.toString();
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          LOGGER.error(StringUtil.extractStackTrace(e));
        }
      }
    }

    return responseJsonString;
  }
}
