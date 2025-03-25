package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.epam.training.gen.ai.service.util.SKUtils;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        return SKUtils.extractAssistantResponse(contextsList);
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

        return SKUtils.extractAssistantResponse(contextsList);
    }


}
