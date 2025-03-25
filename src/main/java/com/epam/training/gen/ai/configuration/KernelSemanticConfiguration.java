package com.epam.training.gen.ai.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.google.cloud.vertexai.VertexAI;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.google.chatcompletion.GeminiChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class KernelSemanticConfiguration {

    @Bean
    @Primary
    public ChatCompletionService chatCompletionService(@Value("${client.azure.openai.model.id}") String modelId, OpenAIAsyncClient client) {
        return OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();
    }

    @Bean
    @Primary
    public Kernel kernel(ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .build();
    }

    /***
     * Please ignore the next bean as {@link VertexAI} is not configured properly in this application
     * This is an example of the component to use anyway
     */
    @Bean("googleChatCompletionService")
    public ChatCompletionService googleChatCompletionService(@Value("${client.google.gemini.model.id}") String modelId, VertexAI vertexAI) {
        return GeminiChatCompletion.builder()
                .withModelId(modelId)
                .withVertexAIClient(vertexAI)
                .build();
    }

    /***
     * Please ignore the next bean as {@link ChatCompletionService} is not configured properly in this application
     * This is an example of the component to use anyway
     */
    @Bean("googleKernel")
    public Kernel googleKernel(@Qualifier("googleChatCompletionService") ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .build();
    }

    @Bean
    @Primary
    public InvocationContext invocationContext() {
        return InvocationContext.builder()
//                .withReturnMode(InvocationReturnMode.FULL_HISTORY)
                .build();
    }

    @Bean
    @Primary
    public ChatHistory chatHistory() {
        return new ChatHistory();
    }
}
