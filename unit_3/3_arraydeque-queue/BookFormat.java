/**
 * Represents the physical or digital format of a book.
 */
public enum BookFormat {
    EBOOK("ebook"),
    PAPERBACK("paperback"),
    HARDBACK("hardback");

    private final String value;

    BookFormat(String value) {
        this.value = value;
    }

    public static BookFormat fromString(String value) {
        for (var format : values()) {
            if (format.value.equals(value.trim())) {
                return format;
            }
        }
        throw new IllegalArgumentException(
            "Format must be 'ebook', 'paperback' or 'hardback', got: '" + value + "'");
    }

    @Override
    public String toString() {
        return value;
    }
}
