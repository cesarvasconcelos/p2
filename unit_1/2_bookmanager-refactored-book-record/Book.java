import java.util.Objects;

/**
 * Book is a pure data carrier: no business logic, no meaningful state mutation after construction.
 * All setters are only called from the constructor. This makes it a perfect candidate for a Java
 * record, which provides immutability, auto-generated equals/hashCode/accessors, and cleaner syntax
 * by design.
 */
public record Book(long code, String title, String author, BookFormat format, double price) {

    /* Compact constructor for validation */
    public Book {
        validateCode(code);
        validateTitle(title);
        validateAuthor(author);
        validateFormat(format);
        validatePrice(price);
    }

    private static void validateCode(long code) {
        if (code <= 0) throw new IllegalArgumentException("Code must be greater than 0");
    }

    private static void validateTitle(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        if (title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
    }

    private static void validateAuthor(String author) {
        Objects.requireNonNull(author, "Author cannot be null");
        if (author.isBlank()) throw new IllegalArgumentException("Author cannot be blank");
    }

    private static void validateFormat(BookFormat format) {
        Objects.requireNonNull(format, "Format cannot be null");
    }

    private static void validatePrice(double price) {
        if (price <= 0 || price > 2500)
            throw new IllegalArgumentException("Price must be between 0 (exclusive) and 2500");
    }

    @Override
    public String toString() {
        return """
               Book[code=%d, title='%s', author='%s', format='%s', price=%.2f]\
               """.formatted(code, title, author, format, price);
    }
}
