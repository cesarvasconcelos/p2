import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages a collection of books using an {@link ArrayDeque} as a <strong>LIFO stack</strong>
 * (Last In, First Out).
 *
 * <h2>The stack mental model</h2>
 * <p>Think of a physical stack of books on a desk. You always place a new book on top,
 * and you always pick up the topmost book first. The last book placed is the first one removed.
 * That is LIFO: <strong>Last In, First Out</strong>.
 *
 * <pre>
 *   push("A")  →  [A]
 *   push("B")  →  [B, A]      ← B is now on top
 *   push("C")  →  [C, B, A]   ← C is now on top
 *   pop()      →  returns C, stack becomes [B, A]
 *   peek()     →  returns B (no removal)
 * </pre>
 *
 * <h2>Why ArrayDeque instead of java.util.Stack?</h2>
 * <p>{@link java.util.Stack} exists in Java but is considered <em>legacy</em>. It extends
 * {@link java.util.Vector}, which is synchronized (thread-safe), making it slower than needed
 * for single-threaded code. The Java documentation explicitly recommends {@link ArrayDeque}
 * as the preferred stack implementation. Both behave identically as stacks; ArrayDeque is
 * simply faster and lighter.
 *
 * <h2>Stack operations on ArrayDeque</h2>
 * <p>When used as a stack, elements are added and removed from the <em>front</em>
 * (the "top") of the deque:
 * <ul>
 *   <li>{@code push(book)} — adds to the front (top of stack); equivalent to {@code addFirst}
 *   <li>{@code pop()}      — removes from the front (top of stack); equivalent to {@code removeFirst}
 *   <li>{@code peek()}     — reads the front without removing; equivalent to {@code peekFirst}
 * </ul>
 *
 * <h2>Use case modeled here: reading history with undo</h2>
 * <p>Each time a student opens a book it is pushed onto their history stack. The most recently
 * opened book sits on top. Calling {@code pop()} undoes the last selection — just like a
 * browser's back button.
 */
public class BookStack {

    // Deque<Book> is the interface; ArrayDeque<Book> is the concrete implementation.
    // Declaring the field as Deque<Book> (the interface) is good practice: it keeps
    // the code decoupled from the specific implementation.
    private final Deque<Book> stack = new ArrayDeque<>();

    private BookStack() {}

    // =======================================================================
    // Factory method
    // =======================================================================

    /**
     * Creates a {@code BookStack} by loading books from a semicolon-delimited file.
     *
     * <p>Each line is parsed into a {@link Book} and pushed onto the stack in file order,
     * so the <em>last line</em> in the file ends up on top of the stack.
     *
     * @param filePath path to the books data file (e.g. {@code "books-data.txt"})
     * @return a fully initialised {@code BookStack}
     * @throws UncheckedIOException if the file cannot be read
     */
    public static BookStack from(String filePath) {
        var bookStack = new BookStack();
        try {
            Files.lines(Path.of(filePath))
                 .filter(line -> !line.isBlank())
                 .map(BookStack::parseLine)
                 .forEach(bookStack::push);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load data file: " + filePath, e);
        }
        return bookStack;
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
    // Stack operations (LIFO)
    // =======================================================================

    /**
     * Pushes a book onto the top of the stack (LIFO).
     *
     * <p>Internally calls {@link Deque#push}, which is equivalent to {@link Deque#addFirst} —
     * the element is placed at the front of the underlying deque, becoming the new top.
     *
     * @param book the book to push; must not be null
     */
    public void push(Book book) {
        // push() = addFirst(): places the element at the FRONT of the deque (the "top" of the stack).
        stack.push(book);
    }

    /**
     * Removes and returns the book on top of the stack (LIFO).
     *
     * <p>Equivalent to {@link Deque#removeFirst}. Throws {@link java.util.NoSuchElementException}
     * if the stack is empty — always check {@link #isEmpty()} first, or catch the exception.
     *
     * @return the most recently pushed book
     */
    public Book pop() {
        // pop() = removeFirst(): removes the element at the FRONT of the deque (the "top").
        return stack.pop();
    }

    /**
     * Returns the book on top of the stack <em>without</em> removing it.
     *
     * <p>Equivalent to {@link Deque#peekFirst}. Returns {@code null} if the stack is empty.
     *
     * @return the most recently pushed book, or {@code null} if the stack is empty
     */
    public Book peek() {
        // peek() = peekFirst(): reads the FRONT element without removing it.
        return stack.peek();
    }

    /** Returns {@code true} if no books are on the stack. */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /** Returns the number of books currently on the stack. */
    public int size() {
        return stack.size();
    }
}
