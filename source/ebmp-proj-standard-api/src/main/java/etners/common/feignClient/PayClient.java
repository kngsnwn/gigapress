package etners.common.feignClient;

import etners.common.config.feign.FeignClientConfig;
import etners.common.feignClient.request.RefundRequest;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
  name = "pay",
  url = "${server.pay.domain}",
  configuration = FeignClientConfig.class)
public interface PayClient {

  @PostMapping(value = "/v1/ebmp/pay/inicis/refund")
  JSONObject refundRequest(@RequestHeader String Authorization, RefundRequest requestParam);


}
