import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookManager {
    private List<Book> books;
    private List<String> lines;

    public BookManager(String filePath) {
        loadFileLines(filePath);
        buildBookList(lines);
    }

    private void loadFileLines(String filePath) {
        try {
            this.lines = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Error loading data file", e);
        }
    }

    private void buildBookList(List<String> lines) {
        books = lines.stream()
                .map(this::parseLineToBook)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Book parseLineToBook(String line) {
        String[] parts = line.split(";");

        return new Book(
                Long.parseLong(parts[0]),
                parts[1],
                parts[2],
                parts[3],
                Double.parseDouble(parts[4])
        );
    }

    public void addBook(Book book) {
        books.addLast(book);
    }

    public void removeBookBy(long code) {
        books.removeIf(bookCodeIsEqualsTo(code));
    }

    public Optional<Book> findBookBy(long code) {
        return books.stream()
                .filter(bookCodeIsEqualsTo(code))
                .findAny();
    }

    private Predicate<Book> bookCodeIsEqualsTo(long code) {
        return book -> book.getCode() == code;
    }
}
