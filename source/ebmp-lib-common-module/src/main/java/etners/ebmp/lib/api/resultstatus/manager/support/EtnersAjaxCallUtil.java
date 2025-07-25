package etners.ebmp.lib.api.resultstatus.manager.support;

import common.util.string.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtnersAjaxCallUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(EtnersAjaxCallUtil.class);

  public String requestAjaxJsonCall(String callUrl) {
    return requestAjaxJsonCall(callUrl, null, "POST");
  }


  public String requestAjaxJsonCall(String callUrl, HashMap<String, Object> paramMap) {
    return requestAjaxJsonCall(callUrl, paramMap, "POST");
  }

  /**
   * 설명	: 백엔드에서 AJAX 호출을 해야할 때 사용할 수 있도록 만든 공통 API. tna-api 쪽의 requestApplicationJson 메서드를 가져왔으며, 일부 로직을 좀 더 알아보기 쉽도록 리팩터링하였다.
   */
  public String requestAjaxJsonCall(String callUrl, HashMap<String, Object> paramMap, String requestMethod) {
    BufferedReader br = null;
    OutputStream os = null;
    String responseJsonString = "No response yet.";

    byte[] jsonParamToByte = null;

    try {
      if (paramMap != null) {
        jsonParamToByte = mapToByte(paramMap);
      }

      //HTTPS initialize
      final SSLContext sslContext = makeSslContext();
      HostnameVerifier hv = makeHostnameVerifier();

      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(hv);

      HttpURLConnection con = initAjaxHttpUrlConnection(callUrl, requestMethod);

      os = con.getOutputStream();

      if (paramMap != null) {
        os.write(jsonParamToByte, 0, jsonParamToByte.length);
      }

      //ajax-call
      br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

      //Response message after ajax-call
      responseJsonString = extractResponseData(br);
    } catch (Exception e) {
      responseJsonString = e.getMessage();
      LOGGER.error(StringUtil.extractStackTrace(e));
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          LOGGER.error(StringUtil.extractStackTrace(e));
        }
      }

      if (os != null) {
        try {
          os.close();
        } catch (Exception e) {
          LOGGER.error(StringUtil.extractStackTrace(e));
        }
      }
    }

    return responseJsonString;
  }

  private byte[] mapToByte(HashMap<String, Object> paramMap) throws UnsupportedEncodingException {
    String jsonParamString = StringUtil.convertMapToJson(paramMap);

    LOGGER.info("\n=================================================="
      + "\n" + "jsonParamString : " + jsonParamString
      + "\n" + "==================================================");

    return jsonParamString.getBytes(StandardCharsets.UTF_8);
  }

  private String extractResponseData(BufferedReader br) throws IOException {
    String responseLine = null;
    StringBuilder responseData = new StringBuilder();

    while ((responseLine = br.readLine()) != null) {
      responseData.append(responseLine.trim());
    }

    return responseData.toString();
  }

  private HostnameVerifier makeHostnameVerifier() {
    HostnameVerifier hv = new HostnameVerifier() {
      public boolean verify(String urlHostName, SSLSession session) {
        return true; // 모든 서버를 신뢰한다
      }
    };

    return hv;
  }

  private SSLContext makeSslContext() throws NoSuchAlgorithmException, KeyManagementException {
    final TrustManager[] trustAllCerts = makeTrustManager();

    // Install the all-trusting trust manager
    final SSLContext sslContext = SSLContext.getInstance("SSL");

    sslContext.init(null, trustAllCerts, new SecureRandom());

    return sslContext;
  }

  private TrustManager[] makeTrustManager() {
    // Create a trust manager that does not validate certificate chains
    final TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      }
    };

    return trustAllCerts;
  }

  private HttpURLConnection initAjaxHttpUrlConnection(String connectionUrl, String requestMethod) throws IOException {
    URL url = new URL(connectionUrl);

    if (requestMethod == null) {
      requestMethod = "POST";
    }

    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod(requestMethod);

    con.setRequestProperty("Content-Type", "application/json; utf-8");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);

    return con;
  }
}
