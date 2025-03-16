package com.epam.training.gen.ai.service.util;

import com.epam.training.gen.ai.exception.NoContentException;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import java.util.List;

public class SKUtils {
    public static String extractAssistantResponse(List<ChatMessageContent<?>> contextsList) {
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
