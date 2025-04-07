package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookInfo;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class BorrowedBookService {
    private final List<BookInfo> borrowedBooksDB = new ArrayList<>();

    public BookInfo findByName(String name) {
        return borrowedBooksDB.stream()
                .filter(book -> book.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getBooksNames() {
        return borrowedBooksDB.stream()
                .map(BookInfo::name)
                .collect(Collectors.toList());
    }

    public void checkoutBook(BookInfo bookInfo) {
        borrowedBooksDB.add(bookInfo);
    }

    public void removeBookByName(String name) {
        borrowedBooksDB.remove(findByName(name));
    }
}
