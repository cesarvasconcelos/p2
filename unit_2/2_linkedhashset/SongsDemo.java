import java.time.Year;

/**
 * Demonstrates order-preserving deduplication with {@link java.util.LinkedHashSet}.
 *
 * <p>The source file {@code worst-songs.txt} contains many duplicate lines. Both
 * {@code HashSet} and {@code LinkedHashSet} remove those duplicates — but only
 * {@code LinkedHashSet} guarantees that the surviving entries are iterated in the
 * order they were <em>first seen</em> in the file.
 *
 * <p>Key observations to notice when running this demo:
 * <ul>
 *   <li>The song count matches {@code 1_hashset-refactored-record} — deduplication is identical.
 *   <li>Songs print in file order (first occurrence): code 1258 appears on line 1, so it is
 *       first; code 1241 appears later, so it follows. With a plain {@code HashSet} this order
 *       would be arbitrary and can vary between JVM runs.
 *   <li>Artists also appear in first-encounter order, making the list predictable for display.
 *   <li>{@code allSongs().getFirst()} and {@code getFirst()} work because {@code SequencedSet}
 *       (Java 21+) exposes positional access that {@code Set} alone does not offer.
 * </ul>
 */
void main() {
    var processor = SongsProcessor.from("worst-songs.txt");

    IO.println("=== Stats ===");
    IO.println("Unique songs  : " + processor.countSongs());
    IO.println("Total artists : " + processor.countArtists());

    IO.println("\n=== All songs in insertion order (first occurrence in file) ===");
    processor.allSongs().forEach(IO::println);

    IO.println("\n=== First and last song by insertion order ===");
    var all = processor.allSongs();
    IO.println("First : " + all.getFirst());
    IO.println("Last  : " + all.getLast());

    IO.println("\n=== Artists in first-appearance order ===");
    processor.getArtists().forEach(IO::println);

    IO.println("\n=== Find by code (Optional) ===");
    processor.findByCode(1258)
             .ifPresentOrElse(
                 song -> IO.println("Found: " + song),
                 ()   -> IO.println("Song not found")
             );

    IO.println("\n=== Find by artist ===");
    processor.findByArtist("Barry Manilow")
             .forEach(IO::println);

    IO.println("\n=== Find by year ===");
    processor.findByYear(Year.of(1977))
             .forEach(IO::println);

    IO.println("\n=== Oldest songs (in insertion order) ===");
    processor.findOldestSongs().forEach(IO::println);

    IO.println("\n=== Most recent songs (in insertion order) ===");
    processor.findMostRecentSongs().forEach(IO::println);
}
