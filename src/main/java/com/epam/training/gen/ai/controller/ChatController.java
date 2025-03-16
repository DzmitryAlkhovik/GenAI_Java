package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ChatResponse;
import com.epam.training.gen.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ChatResponse retrieveAssistantHelp(@RequestParam(name = "input") String userInput) {
        var result = chatService.processInput(userInput);
        return new ChatResponse(result);
    }

    @GetMapping("v2")
    public ChatResponse retrieveAssistantHelpV2(@RequestParam(name = "input") String userInput) {
        var result = chatService.processInputV2(userInput);
        return new ChatResponse(result);
    }

    @GetMapping("output")
    public String retrieveAssistantHelpOutput(@RequestParam(name = "input") String userInput) {
        return chatService.processInput(userInput);
    }

    @GetMapping("history")
    public ChatResponse retrieveAssistantHelpWithHistory(@RequestParam(name = "input") String userInput) {
        var result = chatService.processInputWithHistory(userInput);
        return new ChatResponse(result);
    }
}
