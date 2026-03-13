import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookManager {
    private List<Book> books;

    public BookManager(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        List<String> lines = loadFileLines(filePath);
        buildBookList(lines);
    }

    private List<String> loadFileLines(String filePath) {
        try {
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load data file: " + filePath, e);
        }
    }

    private void buildBookList(List<String> lines) {
        books = lines.stream()
                .map(this::parseLineToBook)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Book parseLineToBook(String line) {
        String[] fields = line.split(";");

        return new Book(
                Long.parseLong(fields[0]),
                fields[1],
                fields[2],
                BookFormat.fromString(fields[3]),
                Double.parseDouble(fields[4])
        );
    }

    public void addBook(Book book) {
        Objects.requireNonNull(book, "Book cannot be null");
        books.addLast(book);
    }

    public void removeBookBy(long code) {
        books.removeIf(bookHasCode(code));
    }

    public Optional<Book> findBookBy(long code) {
        return books.stream()
                .filter(bookHasCode(code))
                .findAny();
    }

    private Predicate<Book> bookHasCode(long code) {
        return book -> book.getCode() == code;
    }
}
