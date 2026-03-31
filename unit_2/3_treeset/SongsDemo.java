import java.time.Year;

/**
 * Demonstrates sorted deduplication and range navigation with {@link java.util.TreeSet}.
 *
 * <p>The source file {@code worst-songs.txt} is the same file used in all previous examples.
 * What changes is the <em>order</em> in which the unique songs appear:
 * <ul>
 *   <li>{@code 0_hashset} — arbitrary (hash-bucket) order, can vary between JVM runs.
 *   <li>{@code 1_hashset-refactored-record} — same arbitrary order.
 *   <li>{@code 2_linkedhashset} — insertion order (first occurrence in the file).
 *   <li><strong>This example</strong> — ascending numeric code order, always, regardless of
 *       file order. The song with code 1005 always comes first; 1860 always comes last.
 * </ul>
 *
 * <h2>Key observations to notice when running this demo</h2>
 * <ul>
 *   <li>The unique song count is identical across all four examples — deduplication logic
 *       is the same. Only the iteration order differs.
 *   <li>{@code NavigableSet.first()} and {@code last()} give the lowest- and highest-code
 *       songs directly, without scanning the whole set.
 *   <li>Artist names print in alphabetical order — a free side-effect of using a
 *       {@code TreeSet<String>}, which sorts {@code String} by its natural ordering.
 *   <li>{@code findReleasedBefore} and {@code findReleasedAfter} show how a sorted backing
 *       store makes range queries natural: instead of scanning all songs, the processor
 *       filters by year while the result set stays sorted by code.
 *   <li>{@code descendingSet()} returns a <em>view</em> of the same data in reverse order —
 *       no copying, no extra memory.
 * </ul>
 */
void main() {
    var processor = SongsProcessor.from("worst-songs.txt");

    IO.println("=== Stats ===");
    IO.println("Unique songs  : " + processor.countSongs());
    IO.println("Total artists : " + processor.countArtists());

    IO.println("\n=== All songs in ascending code order ===");
    processor.allSongs().forEach(IO::println);

    IO.println("\n=== First and last song by code (NavigableSet.first / last) ===");
    var all = processor.allSongs();
    IO.println("First (lowest code)  : " + all.first());
    IO.println("Last  (highest code) : " + all.last());

    IO.println("\n=== Artists in alphabetical order (TreeSet<String> natural ordering) ===");
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

    IO.println("\n=== Range query: songs released before 1975 ===");
    processor.findReleasedBefore(Year.of(1975))
             .forEach(IO::println);

    IO.println("\n=== Range query: songs released in 1980 or later ===");
    processor.findReleasedAfter(Year.of(1980))
             .forEach(IO::println);

    IO.println("\n=== Descending code order (view — no copy) ===");
    processor.allSongs().descendingSet().forEach(IO::println);

    IO.println("\n=== All songs ordered by artist / title / code ===");
    processor.allSongsByArtist().forEach(IO::println);

    IO.println("\n=== All songs ordered by year / code ===");
    processor.allSongsByYear().forEach(IO::println);

    IO.println("\n=== Oldest songs (in code order) ===");
    processor.findOldestSongs().forEach(IO::println);

    IO.println("\n=== Most recent songs (in code order) ===");
    processor.findMostRecentSongs().forEach(IO::println);
}
