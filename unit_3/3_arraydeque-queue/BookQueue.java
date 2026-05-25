import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Manages a collection of books using an {@link ArrayDeque} as a <strong>FIFO queue</strong>
 * (First In, First Out).
 *
 * <h2>The queue mental model</h2>
 * <p>Think of a checkout line at a library. The first person to join the line is served first.
 * New arrivals join at the back. That is FIFO: <strong>First In, First Out</strong>.
 *
 * <pre>
 *   enqueue("A")  →  [A]
 *   enqueue("B")  →  [A, B]      ← B is at the back
 *   enqueue("C")  →  [A, B, C]   ← C is at the back
 *   dequeue()     →  returns A, queue becomes [B, C]
 *   peek()        →  returns B (no removal)
 * </pre>
 *
 * <h2>Why ArrayDeque instead of LinkedList?</h2>
 * <p>Both {@link ArrayDeque} and {@link java.util.LinkedList} implement {@link Deque} and can
 * serve as queues. The difference is memory layout: {@code LinkedList} allocates a separate
 * node object per element (more garbage-collector pressure), while {@code ArrayDeque} stores
 * elements in a resizable array (better cache locality, fewer allocations). For most queue
 * use cases, {@code ArrayDeque} is the faster and leaner choice.
 *
 * <h2>Queue operations on ArrayDeque</h2>
 * <p>When used as a queue, elements enter at the <em>back</em> and leave from the <em>front</em>:
 * <ul>
 *   <li>{@code enqueue(book)} — adds to the back; equivalent to {@code addLast} / {@code offer}
 *   <li>{@code dequeue()}     — removes from the front; equivalent to {@code pollFirst}
 *   <li>{@code peek()}        — reads the front without removing; equivalent to {@code peekFirst}
 * </ul>
 *
 * <h2>Use case modeled here: library cataloguing queue</h2>
 * <p>Newly donated books are loaded into a processing queue. A librarian processes them one by
 * one in arrival order — the first book donated is the first one catalogued. This guarantees
 * fairness: no book waits indefinitely while others jump ahead.
 */
public class BookQueue {

    // Deque<Book> is the interface; ArrayDeque<Book> is the concrete implementation.
    // Declaring the field as Deque<Book> (the interface) is good practice: it keeps
    // the code decoupled from the specific implementation.
    private final Deque<Book> queue = new ArrayDeque<>();

    private BookQueue() {}

    // =======================================================================
    // Factory method
    // =======================================================================

    /**
     * Creates a {@code BookQueue} by loading books from a semicolon-delimited file.
     *
     * <p>Each line is parsed into a {@link Book} and enqueued in file order, so the
     * <em>first line</em> in the file ends up at the front of the queue.
     *
     * @param filePath path to the books data file (e.g. {@code "books-data.txt"})
     * @return a fully initialised {@code BookQueue}
     * @throws UncheckedIOException if the file cannot be read
     */
    public static BookQueue from(String filePath) {
        var bookQueue = new BookQueue();
        try {
            Files.lines(Path.of(filePath))
                 .filter(line -> !line.isBlank())
                 .map(BookQueue::parseLine)
                 .forEach(bookQueue::enqueue);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load data file: " + filePath, e);
        }
        return bookQueue;
    }

    private static Book parseLine(String line) {
        var fields = line.split(";");
        return new Book(
            Long.parseLong(fields[0].trim()),
            fields[1].trim(),
            fields[2].trim(),
            BookFormat.fromString(fields[3].trim()),
            Double.parseDouble(fields[4].trim())
        );
    }

    // =======================================================================
    // Queue operations (FIFO)
    // =======================================================================

    /**
     * Adds a book to the back of the queue (FIFO).
     *
     * <p>Internally calls {@link Deque#addLast} — the element is placed at the end of the
     * underlying deque, where it will wait until all books ahead of it have been dequeued.
     *
     * @param book the book to enqueue; must not be null
     */
    public void enqueue(Book book) {
        // addLast(): places the element at the BACK of the deque (end of the queue).
        queue.addLast(book);
    }

    /**
     * Removes and returns the book at the front of the queue (FIFO).
     *
     * <p>Internally calls {@link Deque#pollFirst}, which returns {@code null} when the queue
     * is empty (unlike {@link Deque#removeFirst}, which throws an exception). Wrapping the
     * result in {@link Optional} makes the "queue was empty" case explicit at every call site.
     *
     * @return an {@link Optional} containing the next book, or empty if the queue is empty
     */
    public Optional<Book> dequeue() {
        // pollFirst(): removes and returns the FRONT element, or null if the deque is empty.
        // Optional.ofNullable wraps null as Optional.empty() so callers never receive null directly.
        return Optional.ofNullable(queue.pollFirst());
    }

    /**
     * Returns the book at the front of the queue <em>without</em> removing it.
     *
     * <p>Internally calls {@link Deque#peekFirst}, which returns {@code null} when the queue
     * is empty. Wrapped in {@link Optional} for null safety.
     *
     * @return an {@link Optional} containing the next book, or empty if the queue is empty
     */
    public Optional<Book> peek() {
        // peekFirst(): reads the FRONT element without removing it; null if empty.
        return Optional.ofNullable(queue.peekFirst());
    }

    /** Returns {@code true} if no books are waiting in the queue. */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /** Returns the number of books currently waiting in the queue. */
    public int size() {
        return queue.size();
    }
}
