import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Loads songs from a semicolon-delimited text file into a {@link TreeSet} and exposes
 * query operations over them.
 *
 * <p>Each line in the source file must follow the format:
 * <pre>
 *   code;title;artist;year
 * </pre>
 *
 * <h2>TreeSet vs LinkedHashSet</h2>
 * <p>All three {@code Set} implementations in this unit series deduplicate entries:
 * <ul>
 *   <li>{@link java.util.HashSet} — no guaranteed order.
 *   <li>{@link java.util.LinkedHashSet} — insertion order (first occurrence wins).
 *   <li>{@link TreeSet} — <strong>sorted order</strong>, determined by a {@link Comparator}.
 * </ul>
 *
 * <p>{@code TreeSet} requires that elements can be compared. Because {@code Song} has no
 * single natural ordering it does not implement {@link Comparable}. Instead, a
 * {@link Comparator} is passed to the constructor: {@link Song#BY_CODE} is chosen here
 * because song codes are unique in the data set, keeping the comparator <em>consistent with
 * equals</em> — it returns {@code 0} only when both songs are logically identical.
 *
 * <h2>NavigableSet</h2>
 * <p>Public methods that return the full collection are declared as {@link NavigableSet}
 * (not {@code TreeSet}) following the principle of programming to the interface
 * (Effective Java §64). {@code NavigableSet} extends {@link java.util.SequencedSet} with
 * range-query and navigation methods: {@code first()}, {@code last()}, {@code headSet()},
 * {@code tailSet()}, {@code subSet()}, {@code descendingSet()}, and
 * {@code descendingIterator()}.
 *
 * <p>Compare with {@code 2_linkedhashset/SongsProcessor.java}: the structural changes are
 * {@code LinkedHashSet} → {@code TreeSet(Song.BY_CODE)}, and return types change from
 * {@link java.util.SequencedSet} to {@link NavigableSet}. Two range-query methods are added
 * to showcase the new capabilities.
 */
public class SongsProcessor {

    private static final int EXPECTED_FIELDS = 4;

    private final NavigableSet<Song> songs;

    private SongsProcessor(NavigableSet<Song> songs) {
        this.songs = songs;
    }

    /**
     * Creates a {@code SongsProcessor} by reading and parsing a song file at the given path.
     *
     * <p>Songs are collected into a {@link TreeSet} ordered by {@link Song#BY_CODE}:
     * duplicates are removed and the remaining entries are always iterated in ascending
     * code order, regardless of the order they appear in the file.
     *
     * @param path the path to the song data file
     * @return a fully initialised {@code SongsProcessor}
     * @throws UncheckedIOException if the file cannot be opened or read
     */
    public static SongsProcessor from(Path path) {
        try {
            var parsed = Files.lines(path)
                    .filter(line -> !line.isBlank())
                    .map(SongsProcessor::parseLine)
                    .toList();

            // TreeSet with Song.BY_CODE: deduplication + ascending-code ordering.
            // BY_CODE is safe as the sole comparator because codes are unique in this
            // data set — it returns 0 only when two songs are logically identical,
            // staying consistent with the record's generated equals method.
            NavigableSet<Song> songs = new TreeSet<>(Song.BY_CODE);
            songs.addAll(parsed);

            return new SongsProcessor(songs);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + path, e);
        }
    }

    /**
     * Convenience overload of {@link #from(Path)} that accepts a plain {@link String} file path.
     *
     * @param path the file path as a string (e.g., {@code "worst-songs.txt"})
     * @return a fully initialised {@code SongsProcessor}
     */
    public static SongsProcessor from(String path) {
        return from(Path.of(path));
    }

    private static Song parseLine(String line) {
        try {
            return createSong(splitDataFrom(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Malformed line (non-numeric code or year): " + line, e);
        }
    }

    private static String[] splitDataFrom(String line) {
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

    /**
     * Returns all unique songs in ascending code order.
     *
     * <p>The returned {@link NavigableSet} is unmodifiable. Use {@code first()} and
     * {@code last()} for positional access, or {@code descendingSet()} for a reverse view.
     */
    public NavigableSet<Song> allSongs() {
        return Collections.unmodifiableNavigableSet(songs);
    }

    /**
     * Finds the song with the given code.
     *
     * <p>Returns an {@link Optional} to make the "not found" case explicit in the signature.
     */
    public Optional<Song> findByCode(int code) {
        return songs.stream()
                .filter(s -> s.code() == code)
                .findFirst();
    }

    /**
     * Finds all songs whose artist name contains the given search term, ignoring letter case.
     * Results are returned in ascending code order (the backing TreeSet order).
     *
     * @param name the artist name fragment to search for (case-insensitive)
     * @return an immutable list of matching songs in code order
     */
    public List<Song> findByArtist(String name) {
        var lower = name.toLowerCase();
        return songs.stream()
                .filter(s -> s.artist().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Finds all songs released in the given year.
     * Results are returned in ascending code order.
     *
     * @param year the release year to filter by
     * @return an immutable list of matching songs in code order
     */
    public List<Song> findByYear(Year year) {
        return songs.stream()
                .filter(s -> s.year().equals(year))
                .toList();
    }

    /**
     * Returns all songs ordered alphabetically by artist name, then by title, then by code.
     *
     * <p>The result is a {@link NavigableSet} backed by a {@link TreeSet} ordered by
     * {@link Song#BY_ARTIST}. Because {@code BY_ARTIST} chains artist → title → code,
     * and codes are unique in the data set, the comparator is consistent with equals —
     * it returns {@code 0} only for logically identical songs, so no entry is silently
     * dropped. The caller gets the full {@code NavigableSet} contract: {@code first()},
     * {@code last()}, {@code descendingSet()}, and range-query methods.
     *
     * @return an unmodifiable {@link NavigableSet} of all songs in artist/title/code order
     */
    public NavigableSet<Song> allSongsByArtist() {
        var result = songs.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_ARTIST)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Returns all songs ordered chronologically by release year, then by code within the
     * same year.
     *
     * <p>The result is a {@link NavigableSet} backed by a {@link TreeSet} ordered by
     * {@link Song#BY_YEAR}. Because {@code BY_YEAR} chains year → code, and codes are
     * unique, the comparator is consistent with equals and no entry is silently dropped.
     *
     * @return an unmodifiable {@link NavigableSet} of all songs in year/code order
     */
    public NavigableSet<Song> allSongsByYear() {
        var result = songs.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_YEAR)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Returns all songs released strictly before the given year, in ascending code order.
     *
     * <p>This demonstrates one of the key advantages of {@link TreeSet} over other
     * {@code Set} implementations: range queries are natural and efficient.
     *
     * @param year the exclusive upper bound (songs from this year are NOT included)
     * @return an unmodifiable {@link NavigableSet} of matching songs in code order
     */
    public NavigableSet<Song> findReleasedBefore(Year year) {
        var result = songs.stream()
                .filter(s -> s.year().isBefore(year))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_CODE)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Returns all songs released in the given year or later, in ascending code order.
     *
     * @param year the inclusive lower bound
     * @return an unmodifiable {@link NavigableSet} of matching songs in code order
     */
    public NavigableSet<Song> findReleasedAfter(Year year) {
        var result = songs.stream()
                .filter(s -> !s.year().isBefore(year))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_CODE)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Returns all songs from the earliest year in the data set, in ascending code order.
     */
    public NavigableSet<Song> findOldestSongs() {
        var oldest = songs.stream()
                .min(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        var result = songs.stream()
                .filter(s -> s.year().equals(oldest.year()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_CODE)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Returns all songs from the most recent year in the data set, in ascending code order.
     */
    public NavigableSet<Song> findMostRecentSongs() {
        var recent = songs.stream()
                .max(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        var result = songs.stream()
                .filter(s -> s.year().equals(recent.year()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Song.BY_CODE)));
        return Collections.unmodifiableNavigableSet(result);
    }

    /**
     * Groups all songs by artist name. Within each group, songs appear in ascending code order.
     *
     * <p>{@code Collectors.groupingBy()} is the stream equivalent of SQL's {@code GROUP BY}.
     */
    public Map<String, List<Song>> groupByArtist() {
        return songs.stream()
                .collect(Collectors.groupingBy(Song::artist));
    }

    /**
     * Returns the distinct set of artist names in <strong>alphabetical order</strong>.
     *
     * <p>{@code String} implements {@link Comparable} with lexicographic ordering, so
     * a {@link TreeSet}{@code <String>} sorts artist names alphabetically without requiring
     * an explicit comparator. Compare with {@code 2_linkedhashset}: there,
     * {@code getArtists()} returned artists in first-appearance order because the backing
     * collection was a {@code LinkedHashSet}. Here the order is always alphabetical.
     */
    public NavigableSet<String> getArtists() {
        return songs.stream()
                .map(Song::artist)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /** Returns the number of unique songs (after deduplication). */
    public long countSongs() {
        return songs.size();
    }

    /** Returns the number of distinct artists. */
    public long countArtists() {
        return songs.stream()
                .map(Song::artist)
                .distinct()
                .count();
    }
}
