package etners.ebmp.lib.api.resultstatus.manager.support;

import common.util.string.StringUtil;
import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import etners.ebmp.lib.enums.lang.EbmpLang;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * : ResultModel에서 메시지 코드를 전달할 때 활용하는 Ajax-Call 클래스. 다른 로직과의 혼용을 막기 위해 백엔드에서 Ajax-Call을 하기 위한 EtnersAjaxCallUtil 클래스를 별도로 구현하였다.
 */
public class EtnersHttpConnector {

  private String connectDomain = null;

  public EtnersHttpConnector() {
  }

  /**
   * 설명   : 윈도우 환경인지 디렉터리 분석을 통해 확인하고, 맞는 경우엔 true, 아니면 false를 리턴한다.
   * <p>
   * 스케줄러가 오로지 리눅스 서버에서만 동작하고, 로컬에서 개발 테스트할 때는 중복에서 동작하지 않도록 처리하기 위함이다.
   */
  public String getExecutionEnvironmentOsSystem() {
    File windowFolder = new File("C:\\Windows");
    File programFilesFolder = new File("C:\\Program Files");

    /** 둘 중 하나의 폴더만 존재해도 윈도우 환경의 컴퓨터로 간주한다. */
    if (windowFolder.isDirectory()
      || programFilesFolder.isDirectory()) {
      return "WINDOW";
    }

    File linuxUsrFolder = new File("/usr");
    File linuxEtcFolder = new File("/etc");
    File linuxBinFolder = new File("/bin");

    /** 리눅스 표준 폴더 구성이므로 셋 다 존재할 때 리눅스 환경의 컴퓨터로 간주한다. */
    if (linuxUsrFolder.isDirectory()
      && linuxEtcFolder.isDirectory()
      && linuxBinFolder.isDirectory()) {
      return "LINUX";
    }

    File maxOsApplicationFolder = new File("/Applications");

    /** 맥 표준 폴더 구성이므로 Applications 폴더가 존재할 때 맥 환경의 컴퓨터로 간주한다. */
    if (maxOsApplicationFolder.isDirectory()) {
      return "MAC";
    }

    return "UNKNOWN";
  }

  /**
   * <pre>
   * 메소드명	: getResultStatus
   * 작성자	: oxide
   * 작성일	: 2019. 8. 2.
   * 설명	:
   * </pre>
   *
   * @param ebmpLang
   * @param messageCode
   * @return
   */
  public ResultStatusG2 getResultStatus(EbmpLang ebmpLang, String messageCode) {
    String osSystem = getExecutionEnvironmentOsSystem();

    if (this.connectDomain == null) {
      switch (osSystem) {
        case "LINUX":
          //리눅스인 경우 최초 접속을 운영쪽 내부 도메인으로 한다.
          //이 소스가 개발/운영에 모두 올라간다고 했을 때 개발은 실패해도 상관없지만
          //운영은 반드시 동작해야한다는 가정 하에 운영의 내부 도메인을 기본값으로 두는 것이다.

          /**
           * 내부 도메인 호출시에는 운영이더라도 http로 호출해야하는 것에 유의.
           */
          this.connectDomain = "http://127.0.0.1:12123/"; //운영 쪽 SINGLE의 내부 호출 URL. SINGLE 운영 서버 내부 포트번호 12123.
          //this.connectDomain = "http://127.0.0.1:10091/"; //개발 쪽 SINGLE의 내부 호출 URL. SINGLE 개발 서버 내부 포트번호 10091.
          break;
        case "WINDOW":
        case "MAC":
        case "UNKNOWN":
          //리눅스가 아닌 경우는 개발 SINGLE 도메인을 호출하도록 처리한다.
          this.connectDomain = "http://dev-ebmp-single.etnersplatform.com/";
          break;
      }
    }

    String targetApi = "/v1/ebmp/code/commonCodeSelectOne.json";

    HashMap<String, String> parameterMap = new HashMap<>();

    parameterMap.put("cmpyCd", "00000");
    parameterMap.put("cdGrup", "217");
    parameterMap.put("cdId", messageCode);

    CommonCodeDetailResponse detailResponse = null;
    ResultStatusG2 resultStatusG2 = null;

    try {
      String responseJsonString = requestApplicationJson(osSystem, targetApi, parameterMap);

      if (responseJsonString == null || "null".equals(responseJsonString) || StringUtil.isEmpty(responseJsonString)) {
        //최초 URL 호출 실패시 다른 도메인을 연결하도록 처리
        switch (osSystem) {
          case "LINUX":
            //리눅스인 경우 개발 내부 도메인을 요청하도록 처리한다. 만약 개발 서버에서 이 소스를 돌렸다면 여기에 도달했을 가능성이 높다.
            this.connectDomain = "http://127.0.0.1:10091/"; //개발 쪽 SINGLE의 내부 호출 URL. SINGLE 개발 서버 내부 포트번호 10091.
            break;
          case "WINDOW":
          case "MAC":
          case "UNKNOWN":
            //이 케이스는 가능성은 매우 낮지만 만일 실패했다면 운영 SINGLE 도메인을 바라보도록 호출해본다.
            this.connectDomain = "https://single.etnersplatform.com/";
            break;
        }

        responseJsonString = requestApplicationJson(osSystem, targetApi, parameterMap);
      }

      detailResponse = jsonStringToCommonCodeDetailResponse(responseJsonString);

      resultStatusG2 = convertCommonCodeDetailToResultStatusG2(ebmpLang, messageCode, detailResponse);
    } catch (Exception e) {

      //최초 URL 호출 실패시 다른 도메인을 연결하도록 처리
      switch (osSystem) {
        case "LINUX":
          //리눅스인 경우 개발 내부 도메인을 요청하도록 처리한다. 만약 개발 서버에서 이 소스를 돌렸다면 여기에 도달했을 가능성이 높다.
          this.connectDomain = "http://127.0.0.1:10091/"; //개발 쪽 SINGLE의 내부 호출 URL. SINGLE 개발 서버 내부 포트번호 10091.
          break;
        case "WINDOW":
        case "MAC":
        case "UNKNOWN":
          //이 케이스는 가능성은 매우 낮지만 만일 실패했다면 운영 SINGLE 도메인을 바라보도록 호출해본다.
          this.connectDomain = "https://single.etnersplatform.com/";
          break;
      }

      return getResultStatus(ebmpLang, messageCode);
    }

    return resultStatusG2;
  }

  private ResultStatusG2 convertCommonCodeDetailToResultStatusG2(EbmpLang ebmpLang, String messageCode, CommonCodeDetailResponse commonCodeDetailResponse) {
    HashMap<EbmpLang, String> messageTextI18N = new HashMap<>();

    String messageKo = commonCodeDetailResponse.getCdNm();
    String messageEn = commonCodeDetailResponse.getCdEn();
    String messageJp = commonCodeDetailResponse.getCdJp();
    String messageZh = commonCodeDetailResponse.getCdZh();
    String messageVn = commonCodeDetailResponse.getCdVn();

    messageTextI18N.put(EbmpLang.KO, messageKo);

    if (StringUtil.isNotEmpty(messageEn)) {
      messageTextI18N.put(EbmpLang.EN, messageEn);
    }

    if (StringUtil.isNotEmpty(messageJp)) {
      messageTextI18N.put(EbmpLang.JP, messageJp);
    }

    if (StringUtil.isNotEmpty(messageZh)) {
      messageTextI18N.put(EbmpLang.ZH, messageZh);
    }

    if (StringUtil.isNotEmpty(messageVn)) {
      messageTextI18N.put(EbmpLang.VN, messageVn);
    }

    return new ResultStatusG2(messageCode, messageTextI18N, ebmpLang);
  }

  private CommonCodeDetailResponse jsonStringToCommonCodeDetailResponse(String responseJsonString) {
    Map<String, String> jsonDataMap = StringUtil.convertJsonToHashMap(responseJsonString);

    CommonCodeDetailResponse commonCodeDetailResponse = new CommonCodeDetailResponse();

    commonCodeDetailResponse.setCmpyCd(jsonDataMap.get("cmpyCd"));
    commonCodeDetailResponse.setCdGrup(jsonDataMap.get("cdGrup"));
    commonCodeDetailResponse.setCdId(jsonDataMap.get("cdId"));
    commonCodeDetailResponse.setCdNm(jsonDataMap.get("cdNm"));
    commonCodeDetailResponse.setCdDesc(jsonDataMap.get("cdDesc"));
    commonCodeDetailResponse.setCdValue(jsonDataMap.get("cdValue"));
    commonCodeDetailResponse.setCdEn(jsonDataMap.get("cdEn"));
    commonCodeDetailResponse.setCdZh(jsonDataMap.get("cdZh"));
    commonCodeDetailResponse.setCdVn(jsonDataMap.get("cdVn"));
    commonCodeDetailResponse.setCdJp(jsonDataMap.get("cdJp"));
    commonCodeDetailResponse.setCdNameSpace(jsonDataMap.get("cdNameSpace"));
    commonCodeDetailResponse.setSelected("true".equals((jsonDataMap.get("selected"))));

    return commonCodeDetailResponse;
  }

  private String requestApplicationJson(String osSystem, String targetApi, HashMap<String, String> parameterMap) throws Exception {
    BufferedReader br = null;
    String responseJsonString = null;

    String connectUrl = this.connectDomain + targetApi;

    //System.out.println("connectUrl : " + connectUrl);

    try {
      StringBuilder responseData = new StringBuilder();

      URL url = new URL(connectUrl);

      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json; utf-8");
      con.setRequestProperty("Accept", "application/json");

      con.setConnectTimeout(5000); //set timeout to 5 seconds

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
      System.err.println("connectUrl : " + connectUrl);

      //URL 호출 실패시 다른 도메인을 연결하도록 처리
      switch (osSystem) {
        case "LINUX":
          //리눅스인 경우 개발 내부 도메인을 요청하도록 처리한다. 만약 개발 서버에서 이 소스를 돌렸다면 여기에 도달했을 가능성이 높다.
          this.connectDomain = "http://127.0.0.1:10091/"; //개발 쪽 SINGLE의 내부 호출 URL. SINGLE 개발 서버 내부 포트번호 10091.
          break;
        case "WINDOW":
        case "MAC":
        case "UNKNOWN":
          //이 케이스는 가능성은 매우 낮지만 만일 실패했다면 운영 SINGLE 도메인을 바라보도록 호출해본다.
          this.connectDomain = "https://single.etnersplatform.com/";
          break;
      }

      return requestApplicationJson(osSystem, targetApi, parameterMap);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          System.err.println("connectUrl : " + connectUrl);
          System.err.println(StringUtil.extractStackTrace(e));
          throw new Exception(e);
        }
      }
    }

    return responseJsonString;
  }

  //통신 후 복원용 이너클래스. 라이브러리에 포함시 활용하기 위함.
  class CommonCodeDetailResponse implements Serializable {

    private static final long serialVersionUID = 6657990161912110539L;

    private String cmpyCd;

    private String cdGrup;

    private String cdId;

    private String cdNm;

    private String cdDesc;

    private String cdValue;

    private String cdEn;

    private String cdZh;

    private String cdVn;

    private String cdJp;

    private String cdNameSpace;

    private boolean selected;

    public CommonCodeDetailResponse() {
    }

    public String getCmpyCd() {
      return cmpyCd;
    }

    public void setCmpyCd(String cmpyCd) {
      this.cmpyCd = cmpyCd;
    }

    public String getCdGrup() {
      return cdGrup;
    }

    public void setCdGrup(String cdGrup) {
      this.cdGrup = cdGrup;
    }

    public String getCdId() {
      return cdId;
    }

    public void setCdId(String cdId) {
      this.cdId = cdId;
    }

    public String getCdNm() {
      return cdNm;
    }

    public void setCdNm(String cdNm) {
      this.cdNm = cdNm;
    }

    public String getCdDesc() {
      return cdDesc;
    }

    public void setCdDesc(String cdDesc) {
      this.cdDesc = cdDesc;
    }

    public String getCdValue() {
      return cdValue;
    }

    public void setCdValue(String cdValue) {
      this.cdValue = cdValue;
    }

    public String getCdEn() {
      return cdEn;
    }

    public void setCdEn(String cdEn) {
      this.cdEn = cdEn;
    }

    public String getCdZh() {
      return cdZh;
    }

    public void setCdZh(String cdZh) {
      this.cdZh = cdZh;
    }

    public String getCdVn() {
      return cdVn;
    }

    public void setCdVn(String cdVn) {
      this.cdVn = cdVn;
    }

    public String getCdJp() {
      return cdJp;
    }

    public void setCdJp(String cdJp) {
      this.cdJp = cdJp;
    }

    public String getCdNameSpace() {
      return cdNameSpace;
    }

    public void setCdNameSpace(String cdNameSpace) {
      this.cdNameSpace = cdNameSpace;
    }

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

  }

}
