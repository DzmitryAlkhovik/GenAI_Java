package com.epam.training.gen.ai.configuration;

import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class GeneralConfiguration {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public PDFTextStripper pdfTextStripper() {
        return new PDFTextStripper();
    }
}
