package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.dto.ChatResponse;
import com.epam.training.gen.ai.service.BookAssistantService;
import com.epam.training.gen.ai.service.BookCartService;
import com.epam.training.gen.ai.service.BorrowedBookService;
import com.epam.training.gen.ai.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("book-assistant")
@RequiredArgsConstructor
public class BookAssistantController {

    private final BookAssistantService bookAssistantService;
    private final BookCartService bookCartService;
    private final BorrowedBookService borrowedBookService;
    private final MemoryService memoryService;

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

    @GetMapping("check/cart")
    public List<BookInfo> checkCart() {
        return bookCartService.getReaderCart();
    }

    @GetMapping("check/borrowed")
    public List<BookInfo> checkBorrowedBooks() {
        return borrowedBookService.getBorrowedBooksDB();
    }

    @GetMapping("memory")
    public String recallFromReadBooks(@RequestParam(name = "input") String userInput) {
        return memoryService.answer(userInput);
    }

    @PostMapping("memory")
    public ResponseEntity<?> readBook(@RequestParam(name = "bookPath") String bookPath) {
        memoryService.readBook(bookPath);
        return ResponseEntity.ok().build();
    }
}
