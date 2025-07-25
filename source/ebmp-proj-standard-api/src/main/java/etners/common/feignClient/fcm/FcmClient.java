package etners.common.feignClient.fcm;

import etners.common.config.feign.FeignClientConfig;
import etners.common.feignClient.fcm.dto.request.SendAlimTalkRequest;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "fcm",
    url = "${server.fcm.domain}",
    configuration = FeignClientConfig.class)
public interface FcmClient {

  @PostMapping(value = "/v1/ebmp/kakao/alimtalk/sendAlimTalkAndSms.json")
  JSONObject sendAlimTalkAndSms(SendAlimTalkRequest requestParam);


}
