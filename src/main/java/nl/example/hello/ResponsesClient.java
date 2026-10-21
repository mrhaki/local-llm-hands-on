package nl.example.hello;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Component
public class ResponsesClient {
  private final RestClient client;
  private final String responsesUrl;
  private final String model;

  public ResponsesClient(
      @Value("${openai.responses-url}") String responsesUrl,
      @Value("${openai.model}") String model) {
    this.responsesUrl = responsesUrl;
    this.model = model;
    var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).build());
    requestFactory.setReadTimeout(Duration.ofSeconds(220));
    var builder = RestClient.builder().requestFactory(requestFactory);
    this.client = builder.build();
  }

  public String respond(String input) {
    return respond(input, false);
  }

  public String respond(String input, boolean disableReasoning) {
    var body = new HashMap<String, Object>(Map.of("model", model, "input", input));
    if (disableReasoning) {
      body.put("reasoning", Map.of("effort", "none"));
    }
    JsonNode response;
    try {
      response = client.post()
          .uri(responsesUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(JsonNode.class);
    } catch (RestClientException exception) {
      throw new RestClientException(
          "De Responses API is niet bereikbaar of gaf een fout terug.", exception);
    }

    var text = new StringBuilder();
    if (response != null) {
      for (JsonNode item : response.path("output")) {
        if (!"message".equals(item.path("type").asString())) continue;
        for (JsonNode content : item.path("content")) {
          if ("output_text".equals(content.path("type").asString())) {
            text.append(content.path("text").asString());
          }
        }
      }
    }
    if (text.isEmpty()) {
      throw new RestClientException("De Responses API gaf geen antwoordtekst terug.");
    }
    return text.toString();
  }
}
