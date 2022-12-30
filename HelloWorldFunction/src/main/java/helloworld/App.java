package helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent,
    APIGatewayProxyResponseEvent> {

  public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input,
                                                    final Context context) {
    Map<String, String> params = input.getQueryStringParameters();
    String siteId = params == null ? "9305" : params.getOrDefault("siteId", "9305");
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("X-Custom-Header", "application/json");
    headers.put("Access-Control-Allow-Headers", "Content-Type");
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Access-Control-Allow-Methods", "GET");

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
        .withHeaders(headers);
    try {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(new URI("https://api.sl.se/api2/realtimedeparturesV4" +
              ".json?key=bfc46803fca3487bbbbcfd727095c2c7&siteid=" + siteId + "&timewindow=60&bus" +
              "=false&tram=false"))
          .GET()
          .build();

      HttpResponse<String> response1 = HttpClient.newBuilder()
          .build()
          .send(request, HttpResponse.BodyHandlers.ofString());

      OffsetDateTime now = OffsetDateTime.now();
      int day = now.getDayOfMonth();
      int minute = now.getMinute();
      int hour = now.getHour();
      String body = "{ \"ResponseData\": { \"Trains\": [ { \"JourneyDirection\": 2, " +
          "\"ExpectedDateTime\": \"2022-08-" + day + "T" + (hour + 2) + ":" + (minute + 20) +
          ":00" +
          "\" " +
          "]" +
          " " +
          "} }";

      return response
          .withStatusCode(200)
          .withBody(response1.body());
    } catch (Exception e) {
      return response
          .withBody("{}")
          .withStatusCode(500);
    }
  }

  private String getPageContents(String address) throws IOException {
    URL url = new URL(address);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
