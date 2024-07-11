package com.wynntech.keycloak;

import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.broker.provider.util.SimpleHttp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public class UserService {

    private final KeycloakSession session;
    private static final Logger LOGGER = Logger.getLogger(UserService.class);

    public UserService(KeycloakSession session) {
        this.session = session;
    }

    public User getUserByUserName(String username) {
        User user = null;
        try {
            String url = "http://localhost:8081/users/" + username;
            SimpleHttp.Response response = SimpleHttp.doGet(url, session)
                .asResponse();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                user = response.asJson(User.class);
            } else {
                LOGGER.error("Response code " + response.getStatus());
            }
        } catch (Exception ex) {
            LOGGER.error("Error fetching user " + username + " from external service: " + ex.getMessage(), ex);
        }
        return user;
    }

    public boolean verifyUserPassword(@PathParam("username") String username, String password) {
        SimpleHttp simpleHttp = SimpleHttp.doPost("http://localhost:8081/users/" + username + "/verify-password", session);

        // Include password as form data in the request body
        simpleHttp.param("password", password);

        // Add headers if needed
        simpleHttp.header("Content-Type", "application/x-www-form-urlencoded");

        boolean verified = false;
        try {
            SimpleHttp.Response response = simpleHttp.asResponse();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                verified = true;
            }
        } catch (Exception e) {
            LOGGER.error("Error verifying user password", e);
        }

        return verified;
    }

    public List<User> searchUsers(String search, Integer firstResult, Integer maxResults) {
        // Construct the URL for the search endpoint
        String url = "http://localhost:8081/users/search?query=" + search;
        try {
            if (firstResult != null) {
                url += "&firstResult=" + firstResult;
            }
            if (maxResults != null) {
                url += "&maxResults=" + maxResults;
            }
            // Send HTTP GET request
            SimpleHttp.Response response = SimpleHttp.doGet(url, session)
                .asResponse();

            if (response.getStatus() == 200) {
                LOGGER.error(response.asString());
                // Convert the response to a list of User objects
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.asString(), new TypeReference<List<User>>() {
                });
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to search users, status: ", e);
        }
        return null;
    }
}
