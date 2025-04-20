package com.epam.training.gen.ai.controller;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.dto.SearchBookInfoDTO;
import com.epam.training.gen.ai.service.EmbeddingService;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("embedding")
@RequiredArgsConstructor
public class EmbeddingController {
    private final EmbeddingService embeddingService;

    @GetMapping("convert")
    public List<EmbeddingItem> retrieveEmbedding(@RequestParam(name = "input") String userInput) {
        return embeddingService.retrieveVector(userInput);
    }

    @PostMapping("book")
    public Points.UpdateStatus saveBook(@RequestParam(name = "input") String userInput,
                                        @RequestBody BookInfo bookInfo) {
        return embeddingService.save(userInput, bookInfo);
    }

    @GetMapping("book")
    public List<SearchBookInfoDTO> retrieveBook(@RequestParam(name = "input") String userInput) {
        return embeddingService.search(userInput);
    }
}
