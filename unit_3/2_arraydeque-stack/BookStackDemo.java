/**
 * Demonstrates {@link BookStack} — an {@link java.util.ArrayDeque} used as a LIFO stack.
 *
 * <h2>Scenario: reading history with undo</h2>
 * <p>Every time a student opens a book it is pushed onto their personal history stack.
 * The most recently opened book always sits on top. Calling {@code pop()} undoes the last
 * selection, just like a browser's back button. The whole history unwinds in reverse order.
 *
 * <h2>Run from the {@code unit_3/2_arraydeque-stack/} directory</h2>
 * <pre>
 *   java --enable-preview --source 25 BookStackDemo.java
 * </pre>
 *
 * <h2>Homework (~20 min)</h2>
 * <ol>
 *   <li><strong>Undo all (loop)</strong> — write a loop that pops every book and prints each
 *       one as it is removed. Observe the reverse order.</li>
 *   <li><strong>Recently viewed list</strong> — each time {@code pop()} is called, instead of
 *       discarding the book add it to a separate {@code List<Book>}. Print the full
 *       "recently viewed" list at the end.</li>
 *   <li><strong>Pop until ebook</strong> — add a method {@code popUntilEbook()} to
 *       {@code BookStack} that keeps popping until it finds a book whose format is
 *       {@code EBOOK}, then returns that book (wrapped in {@code Optional}).</li>
 *   <li><strong>Push all</strong> — add a method {@code pushAll(List<Book> books)} to
 *       {@code BookStack} that pushes every book in the list onto the stack.</li>
 * </ol>
 */
void main() {
    // Load all books from file. Each book is pushed in file order, so the last line
    // in books-data.txt ends up on top of the stack.
    var history = BookStack.from("books-data.txt");

    IO.println("=== Reading History (top = most recently opened) ===");
    IO.println("Books in history : " + history.size());

    // peek() reads the top element without changing the stack.
    IO.println("\n=== peek() — look at top without removing ===");
    IO.println("Currently on top : " + history.peek());
    IO.println("Size after peek  : " + history.size() + "  (unchanged)");

    // pop() removes the top element (LIFO: last pushed is first removed).
    IO.println("\n=== pop() — undo the last book opened ===");
    var undone = history.pop();
    IO.println("Removed          : " + undone);
    IO.println("New top          : " + history.peek());
    IO.println("Size after pop   : " + history.size());

    // push() adds a new book on top.
    IO.println("\n=== push() — open a new book (place it on top) ===");
    var newBook = new Book(999, "Clean Code", "Robert C. Martin", BookFormat.EBOOK, 35.00);
    history.push(newBook);
    IO.println("Pushed           : " + newBook);
    IO.println("Top is now       : " + history.peek());

    // Drain the entire stack in LIFO order to show the reverse-insertion sequence.
    IO.println("\n=== Draining stack — LIFO order (last pushed = first popped) ===");
    var position = 1;
    while (!history.isEmpty()) {
        IO.println("  pop #" + position++ + " → " + history.pop());
    }

    IO.println("\nStack is empty   : " + history.isEmpty());
}
