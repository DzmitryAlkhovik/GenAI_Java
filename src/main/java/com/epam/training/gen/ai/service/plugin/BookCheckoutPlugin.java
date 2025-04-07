package com.epam.training.gen.ai.service.plugin;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.epam.training.gen.ai.service.BookCartService;
import com.epam.training.gen.ai.service.BorrowedBookService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookCheckoutPlugin {
    private final BookCartService bookCartService;
    private final BorrowedBookService borrowedBookService;

    @DefineKernelFunction(
            name = "getBorrowedBooksNames",
            description = "Returns the list of the book names that are listed in the library's data base of borrowed books")
    public List<String> getBorrowedBooksNames() {
        return borrowedBookService.getBooksNames();
    }

    @DefineKernelFunction(
            name = "getBookByName",
            description = "Get information about the book from the library's data base of borrowed books by its name",
            returnType = "com.epam.training.gen.ai.dto.BookInfo",
            returnDescription = "The book from the cart"
    )
    public BookInfo getBookByName(@KernelFunctionParameter(name = "name", description = "The name of the book")
                                  String name) {
        return bookCartService.findByName(name);
    }


    @DefineKernelFunction(
            name = "checkoutBook",
            description = "Checkout a book. Checks if such book is present in the Reader's current cart. If so, add the book to the library's data base of borrowed books and removes it from the Reader's current cart. The method to use when Reader makes his choice"
    )
    public void checkoutBooks(
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name) {
        var book = bookCartService.findByName(name);
        if (Objects.isNull(book)) {
            throw new ServiceWorkException("There is no such book in the cart");
        }
        borrowedBookService.checkoutBook(book);
        bookCartService.removeBookByName(name);
    }

    @DefineKernelFunction(
            name = "removeBookToLibrary",
            description = "Remove the book from the library's data base of borrowed books"
    )
    public void removeBookToLibrary(
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name) {
        borrowedBookService.removeBookByName(name);
    }

    @DefineKernelFunction(
            name = "checkBorrowedBook",
            description = "Verify if the book is present in the data base of borrowed books. Do this before any attempt to remove the book and proceed only if the book is present"
    )
    public boolean checkBorrowedBook(
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name) {
        var book = borrowedBookService.findByName(name);
        return Objects.nonNull(book);
    }
}
