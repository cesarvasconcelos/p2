import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * HOMEWORK — implement a library donation inbox using ArrayDeque as a queue.
 *
 * <h2>Scenario</h2>
 * <p>A library receives book donations throughout the day. Each donation joins an inbox
 * queue and is catalogued in arrival order — the first book to arrive is the first one
 * processed. New arrivals always wait at the back.
 *
 * <h2>Your task</h2>
 * <p>Implement the five methods marked {@code // TODO} below. Each one has a hint and a
 * description of the expected behaviour. Do not change the method signatures or the
 * {@code main()} at the bottom — it tests your implementation automatically.
 *
 * <h2>Run your solution</h2>
 * <pre>
 *   java --enable-preview --source 25 BookInboxHomework.java
 * </pre>
 *
 * <p>When all methods are correct the output will match the {@code // Expected:} comments.
 */
class BookInboxHomework {

    // The donation inbox: first book to arrive sits at the front of the queue.
    // ArrayDeque is used here as a FIFO queue — add to back, remove from front.
    private final Deque<Book> inbox = new ArrayDeque<>();

    // -----------------------------------------------------------------------
    // TODO — implement the methods below
    // -----------------------------------------------------------------------

    /**
     * Adds a newly donated book to the back of the inbox.
     * It will be processed only after all books currently in the queue.
     *
     * <p>Hint: use {@code addLast()} or {@code offer()} on the deque.
     */
    public void receive(Book book) {
        // TODO: add the book to the BACK of the inbox queue
    }

    /**
     * Removes and returns the next book to be processed (the one at the front).
     * Returns {@link Optional#empty()} if the inbox is empty.
     *
     * <p>Hint: {@code pollFirst()} removes the front element and returns {@code null}
     * when the deque is empty (no exception). Wrap the result with
     * {@code Optional.ofNullable(...)}.
     */
    public Optional<Book> processNext() {
        // TODO: remove the FRONT book and return it wrapped in Optional
        return Optional.empty();
    }

    /**
     * Returns the next book to process <em>without</em> removing it from the inbox.
     * Returns {@link Optional#empty()} if the inbox is empty.
     *
     * <p>Hint: use {@code peekFirst()} and wrap with {@code Optional.ofNullable(...)}.
     */
    public Optional<Book> peekNext() {
        // TODO: peek at the FRONT book and return it wrapped in Optional
        return Optional.empty();
    }

    /**
     * Returns the number of books currently waiting in the inbox.
     *
     * <p>Hint: use {@code size()} on the deque.
     */
    public int donationCount() {
        // TODO: return the number of books in the inbox
        return 0;
    }

    /**
     * Processes <em>all</em> remaining books in arrival order.
     * Returns them as a list (first arrived = first in list). The inbox is empty afterwards.
     *
     * <p>Hint: loop while {@code !inbox.isEmpty()}, calling {@code pollFirst()} each time
     * and collecting the results into an {@code ArrayList}.
     */
    public List<Book> processAll() {
        // TODO: dequeue every book and collect them in order into a List<Book>
        return List.of();
    }

    // -----------------------------------------------------------------------
    // Do not modify below — run this to verify your implementation
    // -----------------------------------------------------------------------

    void main() {
        var donationInbox = new BookInboxHomework();

        IO.println("=== Three donations arrive ===");
        donationInbox.receive(new Book(128, "The Adventures of Duck and Scorpion", "Mary Poppins", BookFormat.EBOOK,     10.99));
        donationInbox.receive(new Book(35,  "The Return of Horse and Spider",       "Seth Maven",   BookFormat.HARDBACK,  19.99));
        donationInbox.receive(new Book(12,  "More Fun with Rat and Goose",          "Richard Luke", BookFormat.PAPERBACK, 12.99));
        IO.println("Pending donations: " + donationInbox.donationCount());
        donationInbox.peekNext().ifPresent(b -> IO.println("Next to process  : " + b));
        // Expected:
        //   Pending donations: 3
        //   Next to process  : Book[code=128, title='The Adventures of Duck and Scorpion', author='Mary Poppins', format=ebook, price=10.99]

        IO.println("\n=== Process the first donation ===");
        donationInbox.processNext().ifPresent(b -> IO.println("Processed        : " + b));
        IO.println("Pending donations: " + donationInbox.donationCount());
        // Expected:
        //   Processed        : Book[code=128, title='The Adventures of Duck and Scorpion', author='Mary Poppins', format=ebook, price=10.99]
        //   Pending donations: 2

        IO.println("\n=== New donation arrives while processing ===");
        donationInbox.receive(new Book(999, "Clean Code", "Robert C. Martin", BookFormat.EBOOK, 35.00));
        IO.println("Pending donations: " + donationInbox.donationCount());
        // Expected:
        //   Pending donations: 3

        IO.println("\n=== Process all remaining — FIFO order ===");
        var processed = donationInbox.processAll();
        processed.forEach(b -> IO.println("  → " + b));
        IO.println("Inbox empty      : " + (donationInbox.donationCount() == 0));
        // Expected (FIFO: first arrived = first processed):
        //   → Book[code=35,  title='The Return of Horse and Spider',       author='Seth Maven',      format=hardback,  price=19.99]
        //   → Book[code=12,  title='More Fun with Rat and Goose',          author='Richard Luke',    format=paperback, price=12.99]
        //   → Book[code=999, title='Clean Code',                           author='Robert C. Martin',format=ebook,     price=35.00]
        //   Inbox empty      : true
    }
}
