import java.util.Objects;

public class Book {
    private long code;
    private String title;
    private String author;
    private BookFormat format;
    private double price;

    public Book(long code, String title, String author, BookFormat format, double price) {
        this.setCode(code);
        this.setTitle(title);
        this.setAuthor(author);
        this.setFormat(format);
        this.setPrice(price);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        if (code <= 0) {
            throw new IllegalArgumentException("Code must be greater than 0");
        }
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        Objects.requireNonNull(author, "Author cannot be null");
        if (author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be blank");
        }
        this.author = author;
    }

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(BookFormat format) {
        Objects.requireNonNull(format, "Format cannot be null");
        this.format = format;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0 || price > 2500) {
            throw new IllegalArgumentException("Price must be between 0 (exclusive) and 2500");
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return """
               Book[code=%d, title='%s', author='%s', format='%s', price=%.2f]\
               """.formatted(code, title, author, format, price);
    }
}
