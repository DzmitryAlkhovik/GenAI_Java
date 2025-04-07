package com.epam.training.gen.ai.controller;

import com.azure.ai.openai.models.ImageGenerations;
import com.epam.training.gen.ai.dto.ImageData;
import com.epam.training.gen.ai.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @GetMapping("data")
    public ImageData generateImageData(@RequestParam(name = "input") String userInput) {
        return imageService.generateImageData(userInput);
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateImage(@RequestParam(name = "input") String userInput) {
        return imageService.generateImage(userInput);
    }

    /***
     * Please ignore the next method as the default Semantic Kernel does not work with the provided keys/endpoint
     * This is an example of the component to use anyway
     */
    @GetMapping("base")
    public ImageGenerations defaultApproach(@RequestParam(name = "input") String userInput) {
        return imageService.generateImageSK(userInput);
    }
}
