package com.gigapress.mcp.client;


import io.lettuce.core.dynamic.annotation.Value;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OllamaClient {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    public String generateResponse(String model, String prompt) {
        try {
            var requestBody = new JSONObject()
                    .put("model", model)
                    .put("prompt", prompt)
                    .put("stream", false);

            var request = new Request.Builder()
                    .url(baseUrl + "/api/generate")
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();

            try (var response = client.newCall(request).execute()) {
                var responseBody = response.body().string();
                var json = new JSONObject(responseBody);
                return json.getString("response");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ollama API 호출 실패", e);
        }
    }
}