package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.exception.NoContentException;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final InvocationContext invocationContext;
    private final ChatHistory chatHistory;

    public String processInput(String input) {
        var contextsList = chatCompletionService.getChatMessageContentsAsync(input, kernel, invocationContext).block();

        return extractAssistantResponse(contextsList);
    }

    public String processInputV2(String input) {
        var response = kernel.invokePromptAsync(input).block();

        String result;
        if (response != null && response.getResult() instanceof String) {
            result = (String) response.getResult();
        } else {
            throw new ServiceWorkException("The response is in the wrong format or missed");
        }

        return result;
    }


    public String processInputWithHistory(String input) {
        chatHistory.addUserMessage(input);
        var contextsList = chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContext).block();

        return extractAssistantResponse(contextsList);
    }

    private String extractAssistantResponse(List<ChatMessageContent<?>> contextsList) {
        String result;
        if (contextsList != null) {
            result = contextsList.stream()
                    .filter(contentBlock -> contentBlock.getAuthorRole().equals(AuthorRole.ASSISTANT))
                    .findFirst().orElseThrow(NoContentException::new)
                    .getContent();
        } else {
            throw new ServiceWorkException("Context list for the ChatCompletionService is null");
        }
        return result;
    }
}
