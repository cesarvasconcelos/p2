import java.util.Objects;

/**
 * Immutable data carrier for a book entry.
 *
 * <p>Declared as a Java {@code record}: the compiler automatically generates the canonical
 * constructor, all accessors ({@code code()}, {@code title()}, etc.), {@code equals},
 * {@code hashCode}, and a default {@code toString}. The compact constructor below is the
 * only place we need to add validation logic.
 */
public record Book(long code, String title, String author, BookFormat format, double price) {

    /* Compact constructor — runs inside the generated canonical constructor for validation. */
    public Book {
        if (code <= 0) throw new IllegalArgumentException("Code must be greater than 0");
        Objects.requireNonNull(title, "Title cannot be null");
        if (title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        Objects.requireNonNull(author, "Author cannot be null");
        if (author.isBlank()) throw new IllegalArgumentException("Author cannot be blank");
        Objects.requireNonNull(format, "Format cannot be null");
        if (price <= 0 || price > 2500)
            throw new IllegalArgumentException("Price must be between 0 (exclusive) and 2500");
    }

    @Override
    public String toString() {
        return """
               Book[code=%d, title='%s', author='%s', format=%s, price=%.2f]\
               """.formatted(code, title, author, format, price);
    }
}
