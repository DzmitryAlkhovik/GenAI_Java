package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookInfo;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class BookCartService {
    private final List<BookInfo> readerCart = new ArrayList<>();

    public BookInfo findByName(String name) {
        return readerCart.stream()
                .filter(book -> book.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getBooksNames() {
        return readerCart.stream()
                .map(BookInfo::name)
                .collect(Collectors.toList());
    }

    public void addBookToCart(BookInfo bookInfo) {
        readerCart.add(bookInfo);
    }

    public void removeBookByName(String name) {
        readerCart.remove(findByName(name));
    }
}
