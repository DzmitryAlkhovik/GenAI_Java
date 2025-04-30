package com.epam.training.gen.ai.configuration;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class BookAssistantConfiguration {

    public static final String USER = "Reader";

    @Bean("bookAssistantChatHistory")
    public ChatHistory bookAssistantChatHistory(@Value("classpath:/config/book_assistant_system_prompt.txt") Resource resource) {
        var chatHistory = new ChatHistory();

        try {
            var systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info(systemPrompt);
            chatHistory.addSystemMessage(systemPrompt);
        } catch (IOException e) {
            throw new ServiceWorkException("Error during the loading of the system prompt for the book assistant. No prompt is set", e);
        }

        return chatHistory;
    }

    @Bean("bookAssistantInvocationContext")
    public InvocationContext bookAssistantInvocationContext() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(bookAssistantPromptExecutionSettings())
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

    @Bean("bookAssistantInvocationContextForBookInfo")
    public InvocationContext bookAssistantInvocationContextForBookInfo() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(bookAssistantPromptExecutionSettingsForBookInfo())
//                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

    private PromptExecutionSettings bookAssistantPromptExecutionSettings() {
        return PromptExecutionSettings.builder()
                .withUser(USER) //Set the user to associate with the prompt execution
                .withBestOf(3) //Set the best of setting for prompt execution. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 1.
                .withPresencePenalty(0.4) //"Encourages introducing new ideas and less repetition (MAX: STRONGLY DISCOURAGES REPETITION)" Set the presence penalty setting for prompt execution. The value is clamped to the range [-2.0, 2.0], and the default is 0.0.
                .withFrequencyPenalty(0.4) //"Discourages repeated words or phrases (MAX: STRONGLY PENALIZES REPETITION)" Set the frequency penalty setting for prompt execution. The value is clamped to the range [-2.0, 2.0], and the default is 0.0.
                .withTopP(0.8) //"Considers the top tokens accounting for 80% of the probability distribution, narrowing randomness (MIN: CONSIDERS ONLY THE SINGLE MOST PROBABLE TOKEN)" Set the topP setting for prompt execution. The value is clamped to the range [0.0, 1.0], and the default is 1.0.
                .withTemperature(1.2) //"Adds creativity and randomness to the output (MAX: ADDS HIGH RANDOMNESS)" Set the temperature setting for prompt execution. The value is clamped to the range [0.0, 2.0], and the default is 1.0.
                //.withJsonSchemaResponseFormat() //Set the response format to use a json schema generated for the given class. The name of the response format will be the name of the class.
//                .withResponseFormat(ResponseFormat.Type.TEXT) //Set the response format to use for prompt execution.
                .withResultsPerPrompt(1) //Set the number of results to generate for each prompt. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 1.
                //.withMaxTokens() //Set the maximum number of tokens to generate in the output. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 256.
                //.withModelId() //Set the id of the model to use for prompt execution.
                //.withServiceId() //Set the id of the AI service to use for prompt execution.
                //.withStopSequences() //Set the stop sequences to use for prompt execution.
                //.withTokenSelectionBiases() //Set the token selection biases to use for prompt execution. The bias values are clamped to the range [-100, 100].
                .build();
    }

    private PromptExecutionSettings bookAssistantPromptExecutionSettingsForBookInfo() {
        return PromptExecutionSettings.builder()
                .withUser(USER) //Set the user to associate with the prompt execution
                .withBestOf(3) //Set the best of setting for prompt execution. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 1.
//                .withPresencePenalty(0.4) //"Encourages introducing new ideas and less repetition (MAX: STRONGLY DISCOURAGES REPETITION)" Set the presence penalty setting for prompt execution. The value is clamped to the range [-2.0, 2.0], and the default is 0.0.
//                .withFrequencyPenalty(0.4) //"Discourages repeated words or phrases (MAX: STRONGLY PENALIZES REPETITION)" Set the frequency penalty setting for prompt execution. The value is clamped to the range [-2.0, 2.0], and the default is 0.0.
                .withTopP(0.8) //"Considers the top tokens accounting for 80% of the probability distribution, narrowing randomness (MIN: CONSIDERS ONLY THE SINGLE MOST PROBABLE TOKEN)" Set the topP setting for prompt execution. The value is clamped to the range [0.0, 1.0], and the default is 1.0.
                .withTemperature(0.5) //"Adds creativity and randomness to the output (MAX: ADDS HIGH RANDOMNESS)" Set the temperature setting for prompt execution. The value is clamped to the range [0.0, 2.0], and the default is 1.0.
                .withJsonSchemaResponseFormat(BookInfo.class) //Set the response format to use a json schema generated for the given class. The name of the response format will be the name of the class.
                .withResponseFormat(ResponseFormat.Type.JSON_OBJECT) //Set the response format to use for prompt execution.
                .withResultsPerPrompt(1) //Set the number of results to generate for each prompt. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 1.
                //.withMaxTokens() //Set the maximum number of tokens to generate in the output. The value is clamped to the range [1, Integer.MAX_VALUE], and the default is 256.
                //.withModelId() //Set the id of the model to use for prompt execution.
                //.withServiceId() //Set the id of the AI service to use for prompt execution.
                //.withStopSequences() //Set the stop sequences to use for prompt execution.
                //.withTokenSelectionBiases() //Set the token selection biases to use for prompt execution. The bias values are clamped to the range [-100, 100].
                .build();
    }
}
