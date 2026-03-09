import java.util.StringJoiner;

public class Book {
    private long code;
    private String title;
    private String author;
    private String type;
    private double price;

    public Book(long code, String title, String author, String type, double price) {
        this.setCode(code);
        this.setTitle(title);
        this.setAuthor(author);
        this.setType(type);
        this.setPrice(price);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        if (code > 0) {
            this.code = code;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author != null && !author.isBlank()) {
            this.author = author;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null
                && !type.isBlank()
                && (type.equals("ebook") || type.equals("paperback"))) {
            this.type = type;
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price > 0 && price <= 2500) {
            this.price = price;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Book.class.getSimpleName() + "[", "]")
                .add("code=" + code)
                .add("title='" + title + "'")
                .add("author='" + author + "'")
                .add("type='" + type + "'")
                .add("price=" + price)
                .toString();
    }
}
