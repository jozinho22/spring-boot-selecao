package com.douineau.selecao;

import com.douineau.selecao.auth.AuthRequest;
import com.douineau.selecao.auth.AuthService;
import com.douineau.selecao.model.security.Role;
import com.douineau.selecao.repository.PlayerRepository;
import com.douineau.selecao.repository.security.AuthorizedUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonParser;
;

public class StepDefinitions {

    private String adminJwtToken;
    private String userJwtToken;

    private static final String authURL = "http://localhost:8080/auth/authenticate";
    private static final String registerURL = "http://localhost:8080/auth/register";
    private static final String productsURL = "http://localhost:8080/api/players";

    @Autowired
    private AuthorizedUserRepository uRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private PlayerRepository playerRepo;

    private HttpUriRequest createPostRequest(String URL, AuthRequest authRequest, String jwtToken) throws URISyntaxException, IOException {
        HttpPost httpPost = new HttpPost(URL);

        URI uri = new URIBuilder(httpPost.getURI())
                .build();

        httpPost.setURI(uri);
        httpPost.setEntity(new StringEntity("{\"email\": \""+  authRequest.getEmail() + "\",\"password\": \""+  authRequest.getPassword() + "\"}", ContentType.APPLICATION_JSON));

        HttpUriRequest request = httpPost;
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        prepareBearerToken(jwtToken, request);
        return request;
    }

    private static void prepareBearerToken(String jwtToken, HttpUriRequest request) throws IOException {
        if(jwtToken != null) {
            JsonNode rootNode = new ObjectMapper().readTree(new StringReader(jwtToken));
            String tokenValue = String.valueOf(rootNode.get("token"));
            tokenValue = tokenValue.replace("\"", "");
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
        }
    }

    @Given("I authenticate as ADMIN")
    public void authenticate(DataTable dt) throws URISyntaxException, IOException {

        List<List<String>> rows = dt.asLists(String.class);
        String email = rows.get(0).get(0);
        String password = rows.get(0).get(1);

        AuthRequest authRequest = new AuthRequest(email, password);

        HttpUriRequest request = createPostRequest(authURL, authRequest, null);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);

        adminJwtToken = EntityUtils.toString(httpResponse.getEntity());
        Assert.assertTrue(adminJwtToken != null);

        Map<String, Object> decodedToken = decodeJWT(adminJwtToken);
        Assert.assertTrue(decodedToken.get("sub").equals(email));

        Object roles = decodedToken.get("roles");
        List<LinkedHashMap<String, Object>> rolesCasted = (List<LinkedHashMap<String, Object>>) roles;
        Assert.assertTrue(rolesCasted.size() == 1);

        Assert.assertTrue(rolesCasted.get(0).get("authority").equals("ROLE_" + Role.ADMIN.name()));
    }

    public Map<String, Object> decodeJWT(String jwtToken) throws IOException {
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];
        Base64.Decoder base64Url = Base64.getUrlDecoder();
        Map<String, Object> mapping = new ObjectMapper().readValue(base64Url.decode(base64EncodedBody), HashMap.class);
        return mapping;
    }

    @When("I want to register a new AuthorizedUser, who will have a USER Role")
    public void register(DataTable dt) throws URISyntaxException, IOException {

        List<List<String>> rows = dt.asLists(String.class);
        String email = rows.get(0).get(0);
        String password = rows.get(0).get(1);
        AuthRequest authRequest = new AuthRequest(email, password);

        HttpUriRequest request = createPostRequest(registerURL, authRequest, adminJwtToken);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED);

        userJwtToken = EntityUtils.toString(httpResponse.getEntity());
    }


    @Then("I retrieve a USER token")
    public void retrieveUserToken() throws IOException {

        Assert.assertTrue(userJwtToken != null);

        Map<String, Object> decodedToken = decodeJWT(userJwtToken);

        Object roles = decodedToken.get("roles");
        List<LinkedHashMap<String, Object>> rolesCasted = (List<LinkedHashMap<String, Object>>) roles;
        Assert.assertTrue(rolesCasted.size() == 1);

        Assert.assertTrue(rolesCasted.get(0).get("authority").equals("ROLE_" + Role.USER.name()));

    }

    private HttpUriRequest createGetRequest(String URL, String jwtToken) throws URISyntaxException, IOException {

        HttpGet httpGet = new HttpGet(URL);

        URI uri = new URIBuilder(httpGet.getURI())
                .build();

        httpGet.setURI(uri);

        HttpUriRequest request = httpGet;
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        prepareBearerToken(jwtToken, request);

        return request;
    }

    @Then("my new AuthorizedUser can access to the players list with USER token")
    public void canAccessToProducts() throws URISyntaxException, IOException {
        HttpUriRequest request = createGetRequest(productsURL, userJwtToken);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Then("my new AuthorizedUser can not access to the players list without USER token")
    public void cannotAccessToProducts() throws URISyntaxException, IOException {
        HttpUriRequest request = createGetRequest(productsURL, null);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED);

    }

    @Then("my new AuthorizedUser can not access to the register API with USER token")
    public void cannotAccessToRegister(DataTable dt) throws URISyntaxException, IOException {

        List<List<String>> rows = dt.asLists(String.class);
        String email = rows.get(0).get(0);
        String password = rows.get(0).get(1);

        AuthRequest authRequest = new AuthRequest(email, password);

        HttpUriRequest request = createPostRequest(registerURL, authRequest, userJwtToken);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED);

    }

}
