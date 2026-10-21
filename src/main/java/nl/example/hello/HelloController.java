package nl.example.hello;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@RestController
public class HelloController {
  private final RestClient client;
  private final String responsesUrl;
  private final String model;

  public HelloController(
      @Value("${openai.responses-url}") String responsesUrl,
      @Value("${openai.model}") String model) {
    this.responsesUrl = responsesUrl;
    this.model = model;
    var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).build());
    requestFactory.setReadTimeout(Duration.ofSeconds(120));
    var builder = RestClient.builder().requestFactory(requestFactory);
    this.client = builder.build();
  }

  @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
  public String hello() {
    JsonNode response;
    try {
      response = client.post()
          .uri(responsesUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("model", model, "input", "Say hello world in one short sentence."))
          .retrieve()
          .body(JsonNode.class);
    } catch (RestClientException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
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
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
          "De Responses API gaf geen antwoordtekst terug.");
    }
    return text.toString();
  }
}
