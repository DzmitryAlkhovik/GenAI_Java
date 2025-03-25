package com.epam.training.gen.ai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerations;
import com.epam.training.gen.ai.dto.ImageData;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.epam.training.gen.ai.service.util.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final OpenAIAsyncClient client;
    @Value("client.azure.openai.image.model.id")
    private final String imageModelId;
    private final ApiService apiService;

    public ImageData generateImageData(String input) {
        ImageData imageData;

        try {
            imageData = apiService.generateImage(input);
        } catch (IOException | InterruptedException e) {
            log.error("Image data retrieval problem ", e);
            throw new ServiceWorkException("Something went wrong during the call the AI API", e);
        }

        return imageData;
    }

    public byte[] generateImage(String input) {
        byte[] image;
        try {
            var imageData = apiService.generateImage(input);
            image = apiService.retrieveImage(imageData);
        } catch (IOException | InterruptedException e) {
            log.error("Image data retrieval problem ", e);
            throw new ServiceWorkException("Something went wrong during the call the AI API", e);
        }

        return image;
    }

    /***
     * Please ignore the next method as the default Semantic Kernel does not work with the provided keys/endpoint
     * This is an example of the component to use anyway
     * Example source: <a href="https://learn.microsoft.com/en-us/azure/ai-services/openai/dall-e-quickstart?tabs=command-line%2Ckeyless%2Ctypescript-keyless&pivots=programming-language-java">link</a>
     */
    public ImageGenerations generateImageSK(String input) {
        var imageGenerationOptions = new ImageGenerationOptions(input);
        return client.getImageGenerations(imageModelId, imageGenerationOptions).block();
    }
}
