package com.epam.training.gen.ai.service.util;

import com.epam.training.gen.ai.exception.NoContentException;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatMessageContent;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class SKUtils {
    public static String extractAssistantResponse(List<ChatMessageContent<?>> contextsList) {
        String result;
        if (contextsList != null) {
            result = contextsList.stream()
                    .filter(contentBlock -> contentBlock.getAuthorRole().equals(AuthorRole.ASSISTANT) && Objects.nonNull(contentBlock.getContent()))
                    .findFirst().orElseThrow(NoContentException::new)
                    .getContent();
        } else {
            throw new ServiceWorkException("Context list for the ChatCompletionService is null");
        }
        return result;
    }

    public static void logToolsActions(List<ChatMessageContent<?>> contextsList) {
        if (contextsList != null) {
            var result = contextsList.stream()
                    .filter(OpenAIChatMessageContent.class::isInstance)
                    .map(it -> ((OpenAIChatMessageContent<?>) it).getToolCall())
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(it -> String.format("Plugin(%s)->function(%s)", it.getPluginName(), it.getFunctionName()))
                    .collect(Collectors.toList());

            log.info(">>>> TOOL actions: {}", result);
        } else {
            throw new ServiceWorkException("Context list for the ChatCompletionService is null");
        }
    }
}
