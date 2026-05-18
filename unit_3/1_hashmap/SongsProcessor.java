import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Loads songs from a semicolon-delimited text file into a {@link HashMap} and exposes
 * query and mutation operations that illustrate the {@link Map} API.
 *
 * <p>Each line in the source file must follow the format:
 * <pre>
 *   code;title;artist;year
 * </pre>
 *
 * <h2>Why HashMap instead of HashSet?</h2>
 * <p>In previous examples ({@code unit_2/0_hashset} etc.), songs were stored in a
 * {@link java.util.HashSet}. Finding a song by code required scanning every element — O(n).
 * A {@link HashMap} maps each code directly to its {@code Song}, giving <strong>O(1) average
 * lookup</strong>:
 * <pre>
 *   Map&lt;Integer, Song&gt; songsMap
 *     1258 → Song[code=1258, title="You Light Up My Life", ...]
 *     1245 → Song[code=1245, title="Tie A Yellow Ribbon", ...]
 *     ...
 * </pre>
 *
 * <h2>HashMap ordering</h2>
 * <p>{@code HashMap} gives <strong>no guarantee about iteration order</strong>. Songs visited
 * via {@code songsMap.values()} or {@code songsMap.entrySet()} appear in an arbitrary, hash-bucket
 * order that may change between JVM runs. Compare with {@link java.util.LinkedHashMap}
 * (insertion order) or {@link java.util.TreeMap} (sorted key order).
 *
 * <h2>Java 8+ Map API highlights</h2>
 * <p>The classic {@link Map#get(Object)} returns {@code null} when a key is absent, silently
 * pushing null-handling to every call site. The Java 8 additions make intent explicit:
 * <ul>
 *   <li>{@link Map#getOrDefault(Object, Object)} — the fallback is visible at the call site.
 *   <li>{@link Map#putIfAbsent(Object, Object)} — guards against overwriting existing entries.
 *   <li>{@link Map#computeIfAbsent(Object, java.util.function.Function)} — creates a value only when the key is missing.
 *   <li>{@link Map#computeIfPresent(Object, java.util.function.BiFunction)} — updates a value only when the key exists.
 *   <li>{@link Map#compute(Object, java.util.function.BiFunction)} — unified create-or-update in one call.
 *   <li>{@link Map#replace(Object, Object)} — overwrites an existing entry but never inserts a new one.
 *   <li>{@link Map#entrySet()} — returns {@code Set<Map.Entry<K,V>>} for iterating key and value together.
 * </ul>
 *
 * <p>Each service method in this class highlights one or more of these operations.
 */
public class SongsProcessor {

    private static final int EXPECTED_FIELDS = 4;

    // The backing map: code → Song.
    // HashMap gives O(1) average-case performance for get, put, containsKey, and remove.
    private final Map<Integer, Song> songsMap;

    private SongsProcessor(Map<Integer, Song> songsMap) {
        this.songsMap = songsMap;
    }

    // =======================================================================
    // Factory methods (static — no public constructor)
    // =======================================================================

    /**
     * Creates a {@code SongsProcessor} by reading and parsing a song file at the given path.
     *
     * <p>Duplicates (lines with the same code) are handled by {@link Map#putIfAbsent}:
     * the <em>first</em> occurrence is stored; later occurrences with the same code are
     * silently skipped. Compare with a plain {@code put(code, song)} call, which would
     * overwrite earlier entries — surprising behaviour when the source file contains
     * repeated rows.
     *
     * @param path the path to the song data file
     * @return a fully initialised {@code SongsProcessor}
     * @throws UncheckedIOException if the file cannot be opened or read
     */
    public static SongsProcessor from(Path path) {
        try {
            Map<Integer, Song> songsMap = new HashMap<>();
            Files.lines(path)
                 .filter(line -> !line.isBlank())
                 .map(SongsProcessor::parseLine)
                 .forEach(song ->
                     // putIfAbsent: "insert only when the key is not yet present."
                     // If two lines share the same code, the second call is a no-op.
                     // A plain put(code, song) would silently overwrite the first entry.
                     songsMap.putIfAbsent(song.code(), song)
                 );
            return new SongsProcessor(songsMap);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + path, e);
        }
    }

    /**
     * Convenience overload of {@link #from(Path)} that accepts a plain {@link String} path.
     *
     * @param path the file path as a string (e.g. {@code "worst-songs.txt"})
     * @return a fully initialised {@code SongsProcessor}
     */
    public static SongsProcessor from(String path) {
        return from(Path.of(path));
    }

    // =======================================================================
    // Parsing helpers (private — implementation detail)
    // =======================================================================

    private static Song parseLine(String line) {
        try {
            return createSong(splitDataFrom(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Malformed line (non-numeric code or year): " + line, e);
        }
    }

    private static String[] splitDataFrom(String line) {
        // split(";", -1): the -1 limit preserves trailing empty strings.
        var parts = line.split(";", -1);
        if (parts.length < EXPECTED_FIELDS) {
            throw new IllegalArgumentException("Malformed line (expected 4 fields): " + line);
        }
        return parts;
    }

    private static Song createSong(String[] fields) {
        var code   = Integer.parseInt(fields[0].trim());
        var title  = fields[1].trim();
        var artist = fields[2].trim();
        var year   = Year.of(Integer.parseInt(fields[3].trim()));
        return new Song(code, title, artist, year);
    }

    // =======================================================================
    // Inspection / query methods
    // =======================================================================

    /**
     * Returns all unique songs as an unmodifiable collection.
     *
     * <p>{@link Map#values()} returns a <em>live view</em> of the map's values — not a copy.
     * Wrapping it with {@link Collections#unmodifiableCollection} prevents callers from
     * mutating the map through the returned reference, while avoiding an unnecessary copy.
     *
     * <p>The iteration order is arbitrary (HashMap makes no ordering guarantee).
     */
    public Collection<Song> allSongs() {
        return Collections.unmodifiableCollection(songsMap.values());
    }

    /**
     * Looks up a song by its unique code.
     *
     * <p>This method demonstrates {@link Map#getOrDefault} vs. the classic {@link Map#get}:
     * <ul>
     *   <li>{@code songsMap.get(code)} — returns {@code null} when absent; a silent hazard.
     *   <li>{@code songsMap.getOrDefault(code, null)} — same result, but the chosen fallback
     *       ({@code null}) is <em>visible</em> at the call site instead of being implicit.
     * </ul>
     * <p>Wrapping in {@link Optional} forces call sites to handle both the "found" and
     * "not found" cases explicitly, eliminating the risk of an unguarded {@code null}.
     *
     * @param code the unique song code
     * @return an {@link Optional} containing the song, or empty if the code is unknown
     */
    public Optional<Song> findByCode(int code) {
        // getOrDefault makes the "what to return when absent" decision visible.
        // Compare: Song s = songsMap.get(code); — returns null silently; easy to miss.
        return Optional.ofNullable(songsMap.getOrDefault(code, null));
    }

    /**
     * Returns {@code true} if a song with the given code exists in the songsMap.
     *
     * <p>{@link Map#containsKey} is O(1) for {@link HashMap}. It is preferred over
     * {@code findByCode(code).isPresent()} when only existence matters (not the song itself),
     * because it avoids boxing the result into an {@link Optional}.
     *
     * @param code the song code to check
     */
    public boolean hasCode(int code) {
        return songsMap.containsKey(code);
    }

    /**
     * Returns the number of unique songs loaded (after deduplication).
     *
     * <p>O(1) — backed directly by {@link Map#size()}.
     */
    public int countSongs() {
        return songsMap.size();
    }

    // =======================================================================
    // Analytics / grouping methods
    // =======================================================================

    /**
     * Groups all songs by artist name, returning a map from artist → set of songs.
     *
     * <p>Demonstrates {@link Map#computeIfAbsent} — the idiomatic pattern for building a
     * <em>multi-map</em> (a map where each key maps to a collection of values):
     *
     * <pre>{@code
     * result.computeIfAbsent(artist, _ -> new HashSet<>()).add(song);
     * }</pre>
     *
     * <p><strong>How it works:</strong> if {@code artist} is not yet a key in {@code result},
     * {@code computeIfAbsent} runs the lambda, stores the new empty {@code HashSet}, and
     * returns it. If the artist is already there, it returns the existing set. Either way,
     * the returned set is ready for {@code .add(song)} — in a single chained call.
     *
     * <p>Compare with the two-liner that many students write first:
     * <pre>{@code
     * result.putIfAbsent(artist, new HashSet<>());   // always allocates, even when unnecessary
     * result.get(artist).add(song);                  // two hash lookups instead of one
     * }</pre>
     * {@code computeIfAbsent} avoids the wasted allocation and does only one lookup.
     *
     * @return an unmodifiable map from artist name to the (unordered) set of their songs
     */
    public Map<String, Set<Song>> groupByArtist() {
        var result = new HashMap<String, Set<Song>>();
        for (var song : songsMap.values()) {
            // computeIfAbsent: "if this artist key is missing, create a new HashSet for it."
            // Returns the (existing or newly created) set so we can .add(song) in one shot.
            result.computeIfAbsent(song.artist(), _ -> new HashSet<>()).add(song);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Counts how many unique songs were released in each year.
     *
     * <p>Demonstrates {@link Map#compute}, which handles both the "key absent" (create) and
     * "key present" (update) cases in a single call. The remapping function receives
     * {@code (key, currentValue)}, where {@code currentValue} is {@code null} on first
     * encounter:
     *
     * <pre>{@code
     * result.compute(year, (_, count) -> count == null ? 1L : count + 1);
     * }</pre>
     *
     * <p><strong>Compare with alternatives:</strong>
     * <ul>
     *   <li>{@code getOrDefault + put} — works, but requires two separate hash lookups:
     *       {@code result.put(year, result.getOrDefault(year, 0L) + 1);}
     *   <li>{@code merge(year, 1L, Long::sum)} — most concise for simple counters.
     *       {@code compute} is shown here to make the null-handling logic explicit.
     * </ul>
     *
     * @return an unmodifiable map from release year to unique song count for that year
     */
    public Map<Year, Long> countByYear() {
        var result = new HashMap<Year, Long>();
        for (var song : songsMap.values()) {
            // compute: if year is absent, count is null → start at 1.
            // If year is present, count holds the previous tally → increment.
            result.compute(song.year(), (_, count) -> count == null ? 1L : count + 1);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns all map entries as an unmodifiable set of {@link Map.Entry} objects.
     *
     * <p>This method exposes {@link Map#entrySet()} directly so callers can see the raw
     * {@code Set<Map.Entry<Integer, Song>>} structure. Each {@link Map.Entry} holds a key
     * (the code) and a value (the {@code Song}) as a single object, making it possible to
     * access both without a second map lookup.
     *
     * <p>{@code entrySet()} is a <em>live view</em> of the map — the set and its entries
     * reflect any subsequent puts or removes. Wrapping with
     * {@link Collections#unmodifiableSet} prevents mutation through this view.
     *
     * <p><strong>Typical usage pattern:</strong>
     * <pre>{@code
     * for (Map.Entry<Integer, Song> entry : processor.allEntries()) {
     *     Integer code = entry.getKey();
     *     Song    song = entry.getValue();
     *     ...
     * }
     * }</pre>
     *
     * @return a live, unmodifiable view of all {@code code → Song} map entries
     */
    public Set<Map.Entry<Integer, Song>> allEntries() {
        return Collections.unmodifiableSet(songsMap.entrySet());
    }

    /**
     * Returns the name of the artist who appears most often in this data set.
     *
     * <p>Demonstrates using {@link Map#entrySet()} on a derived map to compare both the key
     * and the value in a single stream pass. Iterating {@code entrySet()} is preferred over
     * iterating {@code keySet()} and calling {@code get(key)} inside the loop — the latter
     * costs a redundant hash lookup per element.
     *
     * <pre>{@code
     * for (var entry : groupByArtist().entrySet()) {
     *     String    artist = entry.getKey();
     *     Set<Song> songs  = entry.getValue();
     *     ...
     * }
     * }</pre>
     *
     * @return the artist name with the highest unique song count
     * @throws NoSuchElementException if no songs have been loaded
     */
    public String topArtistByCount() {
        var byArtist = groupByArtist();
        if (byArtist.isEmpty()) {
            throw new NoSuchElementException("No songs loaded");
        }
        // entrySet() gives both key (artist) and value (set size) in one element.
        // Calling keySet() + get(key) inside the stream would cost an extra hash lookup.
        return byArtist.entrySet().stream()
                .max(Map.Entry.comparingByValue(
                        (a, b) -> Integer.compare(a.size(), b.size())))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    /**
     * Returns a list of summary lines — one per artist — in alphabetical order.
     *
     * <p>Demonstrates chaining {@link Map#entrySet()} with stream operations. The
     * {@link Map.Entry#comparingByKey()} comparator sorts entries by their key (artist name)
     * without extracting the keys into a separate collection first.
     *
     * @return an unmodifiable list of strings, e.g. {@code "Barry Manilow — 2 song(s)"}
     */
    public List<String> artistSummary() {
        // entrySet() + stream: process key-value pairs together in sorted key order.
        return groupByArtist().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> "%s — %d song(s)".formatted(e.getKey(), e.getValue().size()))
                .toList();
    }

    // =======================================================================
    // Mutation methods (demonstrating put/replace/compute/remove)
    // =======================================================================

    /**
     * Adds a song to the songsMap only if its code is not already present.
     *
     * <p>Demonstrates {@link Map#putIfAbsent}: returns the <em>previous</em> value for the key
     * ({@code null} if the key was absent, meaning the song was inserted; the existing value
     * if the key was already there, meaning the song was <em>not</em> inserted).
     *
     * <p>Compare with {@code songsMap.put(code, song)}: a plain {@code put} would silently
     * overwrite any existing song with the same code — a common source of hard-to-trace bugs.
     *
     * @param song the song to add
     * @return {@code true} if the song was newly added; {@code false} if a song with that
     *         code already existed and the map was left unchanged
     */
    public boolean addIfAbsent(Song song) {
        // putIfAbsent returns null  → the key was absent → insertion happened → return true.
        // putIfAbsent returns non-null → key already present → no change → return false.
        return songsMap.putIfAbsent(song.code(), song) == null;
    }

    /**
     * Renames a song's title in place, leaving all other fields unchanged.
     *
     * <p>Demonstrates {@link Map#computeIfPresent}: the remapping function is invoked
     * <em>only</em> when the key exists. If the code is unknown, the map is left untouched
     * and {@link Optional#empty()} is returned — no exception, no side effect.
     *
     * <p>Because {@code Song} is an immutable {@code record}, "updating" a field means
     * creating a new instance with the changed field and replacing the old map entry.
     * The lambda receives {@code (key, existingSong)} and must return the replacement value.
     * Returning {@code null} from the lambda would <strong>remove</strong> the key — so
     * always return a non-null value when the intent is an update, not a deletion.
     *
     * <pre>{@code
     * computeIfPresent(code, (_, existing) ->
     *     new Song(existing.code(), newTitle, existing.artist(), existing.year()))
     * }</pre>
     *
     * @param code     the code of the song to rename
     * @param newTitle the replacement title
     * @return an {@link Optional} containing the updated {@code Song}, or empty if not found
     */
    public Optional<Song> renameTitle(int code, String newTitle) {
        // computeIfPresent fires only if the key is present; returns the new value (or null).
        var updated = songsMap.computeIfPresent(code, (_, existing) ->
                new Song(existing.code(), newTitle, existing.artist(), existing.year()));
        return Optional.ofNullable(updated);
    }

    /**
     * Replaces the song at the given code with an entirely new {@code Song} object.
     *
     * <p>Demonstrates {@link Map#replace(Object, Object)}: behaves like {@link Map#put} but
     * only when the key <em>already exists</em>. If the code is absent, nothing happens and
     * {@link Optional#empty()} is returned. This guards against accidentally inserting a
     * record when the intent is strictly to <strong>update</strong> an existing one.
     *
     * <p>The returned {@link Optional} contains the <em>previous</em> song that was replaced.
     *
     * @param code    the code of the song to replace
     * @param newSong the replacement {@code Song} (its code should match {@code code})
     * @return the previous song wrapped in {@link Optional}, or empty if the code was unknown
     */
    public Optional<Song> replace(int code, Song newSong) {
        // replace(k, v) returns the old value, or null if the key was absent (no insertion).
        return Optional.ofNullable(songsMap.replace(code, newSong));
    }

    /**
     * Removes the song with the given code from the songsMap.
     *
     * <p>Demonstrates {@link Map#remove(Object)}: returns the value that was associated with
     * the key, or {@code null} if the key was not present. Wrapping in {@link Optional}
     * avoids returning {@code null} directly to callers and signals clearly that removal
     * may be a no-op.
     *
     * @param code the code of the song to remove
     * @return an {@link Optional} containing the removed song, or empty if not found
     */
    public Optional<Song> removeSong(int code) {
        return Optional.ofNullable(songsMap.remove(code));
    }
}
