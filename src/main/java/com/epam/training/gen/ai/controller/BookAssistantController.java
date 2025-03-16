package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.dto.ChatResponse;
import com.epam.training.gen.ai.service.BookAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("book-assistant")
@RequiredArgsConstructor
public class BookAssistantController {

    private final BookAssistantService bookAssistantService;

    /**
     * Pure output with no {@link ChatResponse} wrapper for the better view
     */
    @GetMapping("chat")
    public String retrieveAssistantHelp(@RequestParam(name = "input") String userInput) {
        return bookAssistantService.processInput(userInput);
    }

    @GetMapping("book")
    public BookInfo retrieveAssistantHelpForBookInfo(@RequestParam(name = "input") String userInput) {
        return bookAssistantService.processBook(userInput);
    }
}
