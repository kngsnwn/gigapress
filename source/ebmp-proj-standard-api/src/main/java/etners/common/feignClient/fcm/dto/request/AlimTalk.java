package etners.common.feignClient.fcm.dto.request;

import common.util.string.StringUtil;
import etners.common.feignClient.fcm.FcmClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
@Component
@RequiredArgsConstructor
public class AlimTalk {
  private static final Logger LOGGER = LoggerFactory.getLogger(AlimTalk.class);

  private final FcmClient fcmClient;
  public void sendSampleAlimTalk(String sample, boolean idOnlyLogin){
    String solCd="";
    String param01="";
    String param02="";
    String param03="";
    String spTelNo="";
    String cmpyCd="";

    if(StringUtil.isNotEmpty(sample)) {

      String tmpltSn = "";
      String[] messageParam;
        tmpltSn = idOnlyLogin ? "36" : "34";
        messageParam = new String[7];
        messageParam[0] = param01;
        messageParam[1] = param02;
        messageParam[2] = param03;

      SendAlimTalkRequest request = new SendAlimTalkRequest();
      request.setSolCd(solCd);
      request.setToNumber(spTelNo);
      request.setTmpltSn(tmpltSn);
      request.setCmpyCd(cmpyCd);
      request.setMessageParams(messageParam);
      request.setFromNumber("");
      request.setDevSendMessageFl(true);
      request.setSendOnlyAlimTalk(false);
      request.setReservationSendYYYYMMDDHHMI("");
      request.setAlimTalkEventNm("");

      JSONObject jsonResult = fcmClient.sendAlimTalkAndSms(request);
      LinkedHashMap<String, String> resultStatus = (LinkedHashMap<String, String>) jsonResult.get("resultStatus");
      if (!"2000".equals(resultStatus.get("messageCode"))) {
        LOGGER.error("sendPayAlimTalk FCM sendAlimTalkAndSms 실패 ::: " + resultStatus.get("messageText"));
      }
    }
  }
}
