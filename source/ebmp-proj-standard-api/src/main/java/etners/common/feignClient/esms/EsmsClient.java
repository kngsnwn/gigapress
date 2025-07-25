package etners.common.feignClient.esms;

import etners.common.config.feign.FeignClientConfig;
import etners.common.feignClient.esms.dto.request.PrivateKeyRequestParam;
import org.json.simple.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
  name = "esms",
  url = "${server.esms.domain}",
  configuration = FeignClientConfig.class)
public interface EsmsClient {
  @PostMapping("/v1/etners/esms/rsa/private-key")
  JSONObject getPrivateKey(@RequestBody PrivateKeyRequestParam requestParam);

  @GetMapping("/v1/etners/esms/cbmp/rsa/private-key")
  JSONObject getTablePrivateKey(@RequestHeader("Authorization") String authorization, @RequestParam("tableName") String tableName);

  @GetMapping("/v1/etners/esms/cbmp/rsa/private-key")
  JSONObject getRowPrivateKey(@RequestHeader("Authorization") String authorization, @RequestParam("keyPublic") String keyPublic);
}
