package com.epam.training.gen.ai.configuration;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfiguration {

    /***
     * Please ignore the next bean as the provided keys/endpoint is not suitable for the {@link VertexAI}
     * This is an example of the component to use anyway
     */
    @Bean
    public VertexAI vertexAI(@Value("${client.key}") String key, @Value("${client.endpoint}") String endpoint) {
        return new VertexAI.Builder()
                .setProjectId("book-assistant")
                .setLocation("us-east1")
                .setCredentials(GoogleCredentials.create(AccessToken.newBuilder().setTokenValue(key).build()))
                .setApiEndpoint(endpoint)
                .build();
    }
}
