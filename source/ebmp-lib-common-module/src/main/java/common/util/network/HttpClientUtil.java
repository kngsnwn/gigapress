package common.util.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.util.string.StringUtil;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

  public JsonObject post(String url, HashMap<String, Object> parameterMap,
    HashMap<String, Object> headerMap) {
    HttpClientBuilder httpClientBuilder;
    CloseableHttpClient httpClient = null;
    StringEntity params;
    ObjectMapper requestMapper = new ObjectMapper();
    JsonObject jsonObject = null;
    try {
      String jsonString = requestMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(parameterMap);

      httpClientBuilder = HttpClients.custom();
      SSLContext sslcontext = SSLContexts.custom().setProtocol("SSL")
        .loadTrustMaterial(null, (paramArrayOfX509Certificate, paramString) -> true).build();
      httpClientBuilder.setSSLHostnameVerifier(new NoopHostnameVerifier())
        .setSSLContext(sslcontext);
      httpClient = httpClientBuilder.build();

      params = new StringEntity(jsonString, StandardCharsets.UTF_8);

      params.setContentType("application/json");
      params.setContentEncoding("UTF-8");
      params.setChunked(true);

      HttpPost httpPost = new HttpPost(url);
      httpPost.addHeader("Accept", "application/json");
      httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

      if (ObjectUtils.isNotEmpty(headerMap) && headerMap.size() > 0) {
        for (String key : headerMap.keySet()) {
          httpPost.addHeader(key, (String) headerMap.get(key));
        }
      }

      httpPost.setEntity(params);

      CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
      httpResponse.addHeader("Content-Type", "application/json;charset=utf8");

      String responseAsString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

      jsonObject = JsonParser.parseString(responseAsString).getAsJsonObject();
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
      e.printStackTrace();
    } finally {
      if (httpClient != null) {
        try {
          httpClient.close();
        } catch (Exception e) {
          LOGGER.error(StringUtil.extractStackTrace(e));
          e.printStackTrace();
        }
      }
      return jsonObject;
    }
  }
}
