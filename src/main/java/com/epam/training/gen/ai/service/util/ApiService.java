package com.epam.training.gen.ai.service.util;

import com.epam.training.gen.ai.dto.ImageData;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
    public static final String API_KEY_HEADER = "Api-Key";
    public static final String API_ENDPOINT = "/openai/deployments/%s/chat/completions";
    public static final String API_IMAGE_RETRIEVAL_ENDPOINT = "/v1/%s";

    @Value("${client.key}")
    private final String apiKey;
    @Value("${client.endpoint}")
    private final String host;
    @Value("${client.azure.openai.image.model.id}")
    private final String imageModelId;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ImageData generateImage(String prompt) throws IOException, InterruptedException {
        var request = generateHttpRequest(prompt);

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return parseImageResponse(response);
    }

    public byte[] retrieveImage(ImageData data) throws IOException, InterruptedException {
        var request = generateImageHttpRequest(data.source());

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new ServiceWorkException("Failed to retrieve image");
        }

        return response.body();
    }

    private HttpRequest generateHttpRequest(String prompt) throws JsonProcessingException {
        var message = new Message(AuthorRole.USER.name(), prompt);

        var requestBody = new HashMap<>();
        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", 1000);

        var requestJson = objectMapper.writeValueAsString(requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(generateURI(imageModelId)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(API_KEY_HEADER, apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
    }

    private String generateURI(String modelId) {
        return String.format(StringUtils.join(host, API_ENDPOINT), modelId);
    }

    private HttpRequest generateImageHttpRequest(String source) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(generateImageURI(source)))
                .header(API_KEY_HEADER, apiKey)
                .build();
    }

    private String generateImageURI(String imageSource) {
        return String.format(StringUtils.join(host, API_IMAGE_RETRIEVAL_ENDPOINT), imageSource);
    }

    private ImageData parseImageResponse(HttpResponse<String> response) throws JsonProcessingException {
        if (response.statusCode() != 200) {
            throw new ServiceWorkException("Failed to generate image: " + response.body());
        }

        var jsonResponse = objectMapper.readTree(response.body());
        var attachments = jsonResponse.at("/choices/0/message/custom_content/attachments");

        var description = attachments.get(0).get("data").asText();
        var title = attachments.get(1).get("title").asText();
        var type = attachments.get(1).get("type").asText();
        var source = attachments.get(1).get("url").asText();

        return new ImageData(title, description, type, source);
    }

    private record Message(String role, String content) {
    }
}
