import java.time.Year;
import java.util.Map;

/**
 * Demonstrates O(1) key-based lookup and the Java 8+ {@link Map} API using a
 * {@link java.util.HashMap} indexed by song code.
 *
 * <h2>HashMap vs. HashSet — the key difference</h2>
 * <p>In all previous examples ({@code unit_2}), songs were stored in a {@code HashSet}.
 * Finding a song by code meant scanning every element — O(n). A {@code HashMap} stores each
 * song under its code as a key, turning any lookup into an O(1) hash table probe.
 *
 * <h2>What this demo covers</h2>
 * <ul>
 *   <li>{@link java.util.Map#containsKey} — existence check, O(1)
 *   <li>{@link java.util.Map#getOrDefault} — safe lookup with an explicit fallback
 *   <li>{@link java.util.Map#putIfAbsent} — conditional insertion (first-wins deduplication)
 *   <li>{@link java.util.Map#computeIfAbsent} — lazy initialisation of grouped collections
 *   <li>{@link java.util.Map#compute} — unified create-or-update for counters
 *   <li>{@link java.util.Map#computeIfPresent} — conditional update (only when key exists)
 *   <li>{@link java.util.Map#replace} — overwrite without inserting
 *   <li>{@link java.util.Map#remove} — removal by key
 *   <li>{@link java.util.Map#entrySet} — iterating {@code Set<Map.Entry<K,V>>}
 *   <li>{@link java.util.Map#values} — iterating values without keys
 * </ul>
 *
 * <p>Run this demo from {@code unit_3/1_hashmap/} with:
 * <pre>
 *   java --enable-preview --source 25 SongsDemo.java
 * </pre>
 */
void main() {
    var processor = SongsProcessor.from("worst-songs.txt");

    // -----------------------------------------------------------------------
    // Basic stats and values()
    // -----------------------------------------------------------------------
    IO.println("=== Stats ===");
    IO.println("Unique songs loaded: " + processor.countSongs());

    IO.println("\n=== All songs — Map.values() (arbitrary HashMap order) ===");
    // Map.values() returns a live view of the backing map's values.
    // Notice: the order is NOT insertion order and NOT alphabetical — it is determined
    // by each key's hash bucket placement, and can differ between JVM runs.
    processor.allSongs().forEach(IO::println);

    // -----------------------------------------------------------------------
    // containsKey — O(1) existence check
    // -----------------------------------------------------------------------
    IO.println("\n=== containsKey ===");
    IO.println("Has code 1258? " + processor.hasCode(1258));  // true
    IO.println("Has code 9999? " + processor.hasCode(9999));  // false

    // -----------------------------------------------------------------------
    // getOrDefault — safe lookup with explicit fallback
    // -----------------------------------------------------------------------
    IO.println("\n=== getOrDefault (via findByCode → Optional) ===");
    // findByCode uses getOrDefault internally, then wraps the result in Optional.
    // Optional forces the caller to handle both the "found" and "not found" paths.
    processor.findByCode(1258)
             .ifPresentOrElse(
                 s  -> IO.println("Found   : " + s),
                 () -> IO.println("Code 1258 not found"));

    processor.findByCode(9999)
             .ifPresentOrElse(
                 s  -> IO.println("Found   : " + s),
                 () -> IO.println("Code 9999 not found — getOrDefault returned null → Optional.empty"));

    // -----------------------------------------------------------------------
    // computeIfAbsent — lazy initialisation of grouped collections
    // -----------------------------------------------------------------------
    IO.println("\n=== computeIfAbsent — groupByArtist() → Map<String, Set<Song>> ===");
    // computeIfAbsent: "if this artist key is missing, run the lambda to create a new
    // empty HashSet for it." Subsequent songs from the same artist reuse that set.
    // This is the standard pattern for building a multi-map.
    var byArtist = processor.groupByArtist();
    byArtist.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> IO.println(e.getKey() + ": " + e.getValue().size() + " song(s)"));

    // -----------------------------------------------------------------------
    // compute — unified create-or-update
    // -----------------------------------------------------------------------
    IO.println("\n=== compute — countByYear() → Map<Year, Long> ===");
    // compute receives (key, currentValue). currentValue is null on first encounter:
    //   null → start at 1L.  non-null → increment by 1.
    processor.countByYear().entrySet().stream()
             .sorted(Map.Entry.comparingByKey())
             .forEach(e -> IO.println(e.getKey() + ": " + e.getValue() + " song(s)"));

    // -----------------------------------------------------------------------
    // entrySet — iterating Map.Entry<K,V> (key + value together)
    // -----------------------------------------------------------------------
    IO.println("\n=== entrySet — allEntries() → Set<Map.Entry<Integer, Song>> ===");
    // entrySet() is the standard way to iterate when you need both the key and the value.
    // Each Map.Entry<K,V> exposes getKey() and getValue() without a second map lookup.
    // Compare: iterating keySet() and then calling get(key) inside the loop costs an
    // extra hash probe per element.
    IO.println("(showing first 5 entries — order is arbitrary in HashMap)");
    processor.allEntries().stream()
             .limit(5)
             .forEach(e -> IO.println("  code=" + e.getKey() + " → " + e.getValue()));

    IO.println("\n=== entrySet — topArtistByCount() ===");
    // Internally uses entrySet() on the grouped map to compare both key and set.size()
    // in a single stream pass, without a second get() call.
    IO.println("Artist with most songs: " + processor.topArtistByCount());

    IO.println("\n=== entrySet — artistSummary() (sorted alphabetically) ===");
    // Chains entrySet() with Map.Entry.comparingByKey() — no need to extract keys first.
    processor.artistSummary().forEach(IO::println);

    // -----------------------------------------------------------------------
    // putIfAbsent — conditional insertion (first-wins)
    // -----------------------------------------------------------------------
    IO.println("\n=== putIfAbsent — addIfAbsent(Song) ===");
    var newSong = new Song(9999, "Test Song", "Test Artist", Year.of(2024));
    // putIfAbsent: inserts only when the key is absent; returns the old value (null = inserted).
    IO.println("Added code 9999?       " + processor.addIfAbsent(newSong));   // true  — key was absent
    IO.println("Added code 9999 again? " + processor.addIfAbsent(newSong));   // false — key already present
    IO.println("Songs after insertion: " + processor.countSongs());

    // -----------------------------------------------------------------------
    // computeIfPresent — conditional update (fires only when key exists)
    // -----------------------------------------------------------------------
    IO.println("\n=== computeIfPresent — renameTitle(code, newTitle) ===");
    // computeIfPresent: the remapping function runs ONLY if the key is already in the map.
    // Because Song is an immutable record, updating a field means building a new instance.
    processor.renameTitle(9999, "Renamed Test Song")
             .ifPresentOrElse(
                 s  -> IO.println("Renamed  : " + s),
                 () -> IO.println("Code not found — computeIfPresent was a no-op"));

    // With an absent key: the map is untouched and Optional.empty() is returned.
    processor.renameTitle(8888, "Ghost Song")
             .ifPresentOrElse(
                 s  -> IO.println("Renamed  : " + s),
                 () -> IO.println("Code 8888 not found — computeIfPresent skipped (key absent)"));

    // -----------------------------------------------------------------------
    // replace — overwrite without inserting
    // -----------------------------------------------------------------------
    IO.println("\n=== replace — replace(code, newSong) ===");
    var replacement = new Song(9999, "Replaced Song", "Different Artist", Year.of(2025));
    // replace(k, v): works like put(k, v) but ONLY if k already exists.
    // If k is absent, nothing is inserted and null is returned.
    processor.replace(9999, replacement)
             .ifPresentOrElse(
                 old -> IO.println("Replaced. Old song: " + old),
                 ()  -> IO.println("Code not found — replace was a no-op"));

    IO.println("Current song at 9999 : " + processor.findByCode(9999).orElse(null));

    // Trying to replace a code that does not exist — replace is a no-op.
    processor.replace(8888, new Song(8888, "Nope", "Nobody", Year.of(2000)))
             .ifPresentOrElse(
                 old -> IO.println("Replaced: " + old),
                 ()  -> IO.println("Code 8888 absent — replace did NOT insert a new entry"));
    IO.println("Has code 8888 after failed replace? " + processor.hasCode(8888));

    // -----------------------------------------------------------------------
    // remove — removal by key
    // -----------------------------------------------------------------------
    IO.println("\n=== remove — removeSong(code) ===");
    // remove(k): removes the entry and returns the old value, or null if not present.
    processor.removeSong(9999)
             .ifPresentOrElse(
                 s  -> IO.println("Removed  : " + s),
                 () -> IO.println("Code not found — nothing removed"));

    IO.println("Has code 9999 after removal? " + processor.hasCode(9999));
    IO.println("Songs after removal  : " + processor.countSongs());
}
