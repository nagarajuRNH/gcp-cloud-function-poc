package com.example.functions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;

public class Main implements HttpFunction {

    private static final Logger LOGGER = Logger.getLogger("GCP Cloud Function");

    private static final String CONTENT_TYPE = "application/json";

    private boolean checkNullOrEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    private void setResponse(int statusCode, String responseBody, HttpResponse response)
            throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatusCode(statusCode);
        BufferedWriter responseWriter = response.getWriter();
        responseWriter.write(responseBody);
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        LOGGER.info("Starting function");
        Gson gson = new Gson();
        Map<String, String> responseMap = new HashMap<>();
        String httpMethod = request.getMethod();
        if (!httpMethod.equalsIgnoreCase("post")) {
            responseMap.put("error", "This endpoint support only a 'POST' request");
            String responseStr = gson.toJson(responseMap);
            setResponse(405, responseStr, response);
            LOGGER.info("This endpoint support only a 'POST' request");
            return;
        }
        InputStream requestStream = request.getInputStream();
        String requestString = IOUtils.toString(requestStream, StandardCharsets.UTF_8);
        User user = gson.fromJson(requestString, User.class);
        if (!checkNullOrEmpty(user.getUsername())) {
            responseMap.put("error", "username is required");
            String responseStr = gson.toJson(responseMap);
            LOGGER.info("Bad Request. Username is empty or null");
            setResponse(400, responseStr, response);
            return;
        }
        if (!checkNullOrEmpty(user.getPhone())) {
            responseMap.put("error", "phone is required");
            String responseStr = gson.toJson(responseMap);
            LOGGER.info("Bad Request. Phone is empty or null");
            setResponse(400, responseStr, response);
            return;
        }
        responseMap.put("uuid", UUID.randomUUID().toString());
        LOGGER.info("Response Map: " + responseMap);
        String responseStr = gson.toJson(responseMap);
        LOGGER.info("Success.");
        setResponse(200, responseStr, response);
    }
}