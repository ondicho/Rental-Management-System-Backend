package ondicho.co.ke.RentalManangementSystemBackend.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.DarajaAccessToken;
//import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static String authorizationUrl = null;
    private static String token = null;

    public HttpUtil(
            @Value("${daraja.sandbox.authorization}") String authorizationUrl,
            @Value("${daraja.base64.token}") String token
    ) {
        this.authorizationUrl = authorizationUrl;
        this.token = token;
    }

    public static DarajaAccessToken authenticate() {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(authorizationUrl);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Basic " + token);
            HttpResponse response = httpClient.execute(request);
            Gson gson = new Gson();
            String responseBody = EntityUtils.toString(response.getEntity());
            DarajaAccessToken darajaAccessToken = gson.fromJson(responseBody, DarajaAccessToken.class);
            return darajaAccessToken;
        } catch (Exception exception) {
            LOGGER.error("Error validating token: {}", exception.getMessage(), exception); // Log the exception
            return null;
        }
    }

    public static Map<String, Object> darajaRequest(String url, Object body) {
        try {

            DarajaAccessToken token = authenticate();
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(url);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + token.getAccess_token());
            Gson gson = new Gson();
            String jsonBody = gson.toJson(body);
            StringEntity entity = new StringEntity(jsonBody);
            LOGGER.info("daraja request body : " + jsonBody);
            request.setEntity(entity);
            LOGGER.info("daraja request  : " + request);
            LOGGER.info("daraja token  : " + token.getAccess_token());
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            if (statusCode == HttpStatus.SC_OK) {
                LOGGER.info("daraja request response : " + responseBody);
                Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {
                }.getType());
                return responseMap;
            } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                LOGGER.error("Bad Request: " + response.getStatusLine().getReasonPhrase());
                LOGGER.info("daraja request response : " + responseBody);
                return null;
            } else {
                LOGGER.error("Unexpected status code: " + statusCode);
                LOGGER.info("daraja request response : " + responseBody);
                return null;
            }
        } catch (Exception exception) {
            LOGGER.error("Error validating token: {}", exception.getMessage(), exception); // Log the exception
            return null;
        }
    }


}
