package ${packageName}.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ${entityName}RestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "${r"${"}external.api.${entityName?lower_case}.url:http://localhost:8080}";

    @Retry(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public <T> T get(String endpoint, Class<T> responseType, Map<String, String> params) {
        log.info("Making GET request to: {}", endpoint);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint);
        params.forEach(builder::queryParam);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<T> response = restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.GET,
            entity,
            responseType
        );
        
        return response.getBody();
    }

    @Retry(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType) {
        log.info("Making POST request to: {}", endpoint);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<R> response = restTemplate.postForEntity(
            baseUrl + endpoint,
            entity,
            responseType
        );
        
        return response.getBody();
    }

    @Retry(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public <T> void put(String endpoint, T requestBody) {
        log.info("Making PUT request to: {}", endpoint);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);
        
        restTemplate.exchange(
            baseUrl + endpoint,
            HttpMethod.PUT,
            entity,
            Void.class
        );
    }

    @Retry(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void delete(String endpoint) {
        log.info("Making DELETE request to: {}", endpoint);
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        restTemplate.exchange(
            baseUrl + endpoint,
            HttpMethod.DELETE,
            entity,
            Void.class
        );
    }
}
