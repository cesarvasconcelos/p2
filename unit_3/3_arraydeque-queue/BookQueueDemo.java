/**
 * Demonstrates {@link BookQueue} — an {@link java.util.ArrayDeque} used as a FIFO queue.
 *
 * <h2>Scenario: library book cataloguing queue</h2>
 * <p>Newly donated books arrive at a library and are placed in a processing queue. A librarian
 * works through the queue one book at a time, always taking the next book from the front.
 * The first book to arrive is the first one catalogued — a fair, ordered process.
 *
 * <h2>Run from the {@code unit_3/3_arraydeque-queue/} directory</h2>
 * <pre>
 *   java --enable-preview --source 25 BookQueueDemo.java
 * </pre>
 *
 * <h2>Homework (~20 min)</h2>
 * <ol>
 *   <li><strong>Process all in order (loop)</strong> — write a loop that dequeues every book
 *       and prints each one with its position number. Observe the FIFO order.</li>
 *   <li><strong>Drain only ebooks</strong> — add a method {@code dequeueAllEbooks()} to
 *       {@code BookQueue} that removes and returns only {@code EBOOK}-format books as a
 *       {@code List<Book>}, leaving all other books in the queue.</li>
 *   <li><strong>Batch limit</strong> — add a method {@code dequeueUpTo(int limit)} that
 *       removes at most {@code limit} books from the front and returns them as a
 *       {@code List<Book>}.</li>
 *   <li><strong>Filter re-queue</strong> — dequeue all books, keep only those priced under
 *       $20, and re-enqueue them in the same arrival order. Print both the discarded and
 *       the re-enqueued books.</li>
 * </ol>
 */
void main() {
    // Load all books from file. Each book is enqueued in file order, so the first line
    // in books-data.txt ends up at the front of the queue.
    var catalogueQueue = BookQueue.from("books-data.txt");

    IO.println("=== Library Cataloguing Queue (front = next to process) ===");
    IO.println("Books waiting    : " + catalogueQueue.size());

    // peek() reads the front element without changing the queue.
    IO.println("\n=== peek() — look at front without removing ===");
    catalogueQueue.peek()
                  .ifPresent(book -> IO.println("Next in line     : " + book));
    IO.println("Size after peek  : " + catalogueQueue.size() + "  (unchanged)");

    // dequeue() removes the front element (FIFO: first enqueued is first returned).
    IO.println("\n=== dequeue() — process the next book in line ===");
    catalogueQueue.dequeue()
                  .ifPresent(book -> IO.println("Catalogued       : " + book));
    IO.println("New front        : " + catalogueQueue.peek().orElse(null));
    IO.println("Size after dequeue: " + catalogueQueue.size());

    // enqueue() adds a new book at the back — it will wait behind all current books.
    IO.println("\n=== enqueue() — new donation arrives at the back ===");
    var donation = new Book(999, "Clean Code", "Robert C. Martin", BookFormat.EBOOK, 35.00);
    catalogueQueue.enqueue(donation);
    IO.println("Enqueued         : " + donation);
    IO.println("Still at front   : " + catalogueQueue.peek().orElse(null));
    IO.println("Queue size now   : " + catalogueQueue.size());

    // Drain the entire queue in FIFO order to show the original-insertion sequence.
    IO.println("\n=== Draining queue — FIFO order (first enqueued = first dequeued) ===");
    var position = 1;
    while (!catalogueQueue.isEmpty()) {
        var book = catalogueQueue.dequeue().orElseThrow();
        IO.println("  dequeue #" + position++ + " → " + book);
    }

    IO.println("\nQueue is empty   : " + catalogueQueue.isEmpty());
}
