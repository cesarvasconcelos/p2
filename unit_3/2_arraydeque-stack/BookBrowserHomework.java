import java.util.ArrayDeque;
import java.util.Deque;

/**
 * HOMEWORK — implement a book browsing history using ArrayDeque as a stack.
 *
 * <h2>Scenario</h2>
 * <p>You are building a history feature for a digital book catalogue. Every time a
 * user opens a book it is recorded in their history. They can "go back" to the
 * previous book, just like a web browser's back button.
 *
 * <h2>Your task</h2>
 * <p>Implement the six methods marked {@code // TODO} below. Each one has a hint and a
 * description of the expected behaviour. Do not change the method signatures or the
 * {@code main()} at the bottom — it tests your implementation automatically.
 *
 * <h2>Run your solution</h2>
 * <pre>
 *   java --enable-preview --source 25 BookBrowserHomework.java
 * </pre>
 *
 * <p>When all methods are correct the output will match the {@code // Expected:} comments.
 */
class BookBrowserHomework {

    // The browsing history: the most recently opened book sits at the top (front).
    // ArrayDeque is used here as a LIFO stack — push to front, pop from front.
    private final Deque<Book> history = new ArrayDeque<>();

    // -----------------------------------------------------------------------
    // TODO — implement the methods below
    // -----------------------------------------------------------------------

    /**
     * Records that the user opened this book.
     * After calling {@code open(book)}, {@link #currentBook()} must return this book.
     *
     * <p>Hint: use {@code push()} or {@code addFirst()} on the deque.
     */
    public void open(Book book) {
        // TODO: push book onto the history stack
    }

    /**
     * Goes back: removes the current book from history and returns it.
     * Returns {@code null} if there is no history to go back to.
     *
     * <p>Hint: check {@code isEmpty()} first. If empty return {@code null}.
     * Otherwise use {@code pop()} or {@code removeFirst()}.
     */
    public Book goBack() {
        // TODO: remove and return the top book, or return null if empty
        return null;
    }

    /**
     * Returns the book currently on top of history <em>without</em> removing it.
     * Returns {@code null} if history is empty.
     *
     * <p>Hint: use {@code peek()} or {@code peekFirst()}.
     */
    public Book currentBook() {
        // TODO: peek at the top of the stack (no removal)
        return null;
    }

    /**
     * Returns how many books are in the browsing history.
     *
     * <p>Hint: use {@code size()} on the deque.
     */
    public int historySize() {
        // TODO: return the number of books in history
        return 0;
    }

    /**
     * Returns {@code true} if there are no books in the history.
     *
     * <p>Hint: use {@code isEmpty()} on the deque.
     */
    public boolean isEmpty() {
        // TODO: delegate to the deque's isEmpty()
        return true;
    }

    /**
     * Clears all browsing history.
     *
     * <p>Hint: use {@code clear()} on the deque.
     */
    public void clearHistory() {
        // TODO: empty the history stack
    }

    // -----------------------------------------------------------------------
    // Do not modify below — run this to verify your implementation
    // -----------------------------------------------------------------------

    void main() {
        var browser = new BookBrowserHomework();

        IO.println("=== Open three books ===");
        browser.open(new Book(128, "The Adventures of Duck and Scorpion", "Mary Poppins",  BookFormat.EBOOK,      10.99));
        browser.open(new Book(35,  "The Return of Horse and Spider",       "Seth Maven",    BookFormat.HARDBACK,   19.99));
        browser.open(new Book(12,  "More Fun with Rat and Goose",          "Richard Luke",  BookFormat.PAPERBACK,  12.99));
        IO.println("Currently on   : " + browser.currentBook());
        IO.println("History size   : " + browser.historySize());
        // Expected:
        //   Currently on   : Book[code=12, title='More Fun with Rat and Goose', author='Richard Luke', format=paperback, price=12.99]
        //   History size   : 3

        IO.println("\n=== Go back once ===");
        var left = browser.goBack();
        IO.println("Left behind    : " + left);
        IO.println("Now on         : " + browser.currentBook());
        IO.println("History size   : " + browser.historySize());
        // Expected:
        //   Left behind    : Book[code=12, title='More Fun with Rat and Goose', author='Richard Luke', format=paperback, price=12.99]
        //   Now on         : Book[code=35, title='The Return of Horse and Spider', author='Seth Maven', format=hardback, price=19.99]
        //   History size   : 2

        IO.println("\n=== Go back until history is empty ===");
        while (!browser.isEmpty()) {
            IO.println("Going back from: " + browser.goBack());
        }
        IO.println("History empty  : " + browser.isEmpty());
        // Expected:
        //   Going back from: Book[code=35, title='The Return of Horse and Spider', author='Seth Maven', format=hardback, price=19.99]
        //   Going back from: Book[code=128, title='The Adventures of Duck and Scorpion', author='Mary Poppins', format=ebook, price=10.99]
        //   History empty  : true

        IO.println("\n=== goBack() on empty history returns null ===");
        IO.println("Result         : " + browser.goBack());
        // Expected:
        //   Result         : null
    }
}
