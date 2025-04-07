package com.epam.training.gen.ai.service.plugin;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.service.BookCartService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookCartPlugin {

    public static final String COMMA_CHAR = ",";
    private final BookCartService bookCartService;

    /**
     * In general OpenAI returns the correct answer but there is always an exception that it cannot convert the String to the com.epam.training.gen.ai.dto.BookInfo
     */
//    @DefineKernelFunction(
//            name = "getReaderCart",
//            description = "Returns the books information from the Reader's current cart, including the total items in the cart",
//            returnType = "java.util.List",
//            returnDescription = "The list of objects of com.epam.training.gen.ai.dto.BookInfo",
//            samples = {@SKSample(inputs = "", output = "[{\"name\":\"A Study in Scarlet\",\"author\":\"Arthur Conan Doyle\",\"year\":\"1887\",\"genres\":[],\"annotation\":\"Classic detective stories featuring Sherlock Holmes and Dr. Watson.\"}]")}
//    )
//    public List<BookInfo> getReaderCart(){
//        return bookCartService.getReaderCart();
//    }
    @DefineKernelFunction(
            name = "getBookNamesFromCart",
            description = "Returns the list of the books from the Reader's current cart",
            returnType = "java.util.List",
            returnDescription = "The list of book names from the cart")
    public List<String> getBookNamesFromCart() {
        return bookCartService.getBooksNames();
    }

    @DefineKernelFunction(
            name = "getBookByName",
            description = "Returns the book from the Reader's current cart by its name",
            returnType = "com.epam.training.gen.ai.dto.BookInfo",
            returnDescription = "The book from the cart"
    )
    public BookInfo getBookByName(@KernelFunctionParameter(name = "name", description = "The name of the book")
                                  String name) {
        return bookCartService.findByName(name);
    }


    @DefineKernelFunction(
            name = "addBookToCart",
            description = "Add a book to the Reader's current cart. The method to use when Reader wants to take the book"
    )
    public void addBookToCart(
//            @KernelFunctionParameter(
//                    name = "bookInfo",
//                    description = "The information about the book",
//                    type = com.epam.training.gen.ai.dto.Book.class,
//                    required = true
//            )
//            BookInfo bookInfo
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name,
            @KernelFunctionParameter(name = "author", description = "The author of the book")
            String author,
            @KernelFunctionParameter(name = "year", description = "The publication year of the book")
            String year,
            @KernelFunctionParameter(name = "genres", description = "The list of genres of the book. The list elements are joined by comma with no spaces")
            String genres,
            @KernelFunctionParameter(name = "annotation", description = "The annotation to the book")
            String annotation) {
        bookCartService.addBookToCart(new BookInfo(name, author, year, transformToList(genres), annotation));
    }

    @DefineKernelFunction(
            name = "removeBookFromCart",
            description = "Remove the book from the Reader's current cart"
    )
    public void removeBookFromCart(
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name) {
        bookCartService.removeBookByName(name);
    }


    @DefineKernelFunction(
            name = "checkBookInTheCart",
            description = "Verify if the book is present in the Reader's current cart. Do this before any attempt to remove the book and proceed only if the book is present"
    )
    public boolean checkBookInTheCart(
            @KernelFunctionParameter(name = "name", description = "The name of the book")
            String name) {
        var book = bookCartService.findByName(name);
        return Objects.nonNull(book);
    }

    private static List<String> transformToList(String genres) {
        return Arrays.stream(genres.split(COMMA_CHAR))
                .map(String::trim)
                .toList();
    }
}
