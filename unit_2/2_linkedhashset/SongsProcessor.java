import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.stream.Collectors;

/**
 * Loads songs from a semicolon-delimited text file into a {@link LinkedHashSet} and exposes
 * query operations over them.
 *
 * <p>Each line in the source file must follow the format:
 * <pre>
 *   code;title;artist;year
 * </pre>
 *
 * <h2>LinkedHashSet vs HashSet</h2>
 * <p>Both {@link java.util.HashSet} and {@link LinkedHashSet} deduplicate entries using
 * {@code equals}/{@code hashCode} — duplicates in the source file are silently dropped.
 * The difference is <em>iteration order</em>:
 *
 * <ul>
 *   <li>{@code HashSet} — no guaranteed order; iteration order is determined by hash buckets
 *       and can change between JVM runs or after rehashing.
 *   <li>{@code LinkedHashSet} — maintains <strong>insertion order</strong>: elements are
 *       iterated in the order they were first added. A duplicate entry does not update the
 *       position of the original.
 * </ul>
 *
 * <p>The practical consequence here: after loading, songs are always iterated in the order
 * they appear in the file (first occurrence wins). This makes the program's output
 * deterministic and predictable — important for demos, reports, and UI display.
 *
 * <p>Methods that expose collection-level ordering return {@link SequencedSet} (introduced in
 * Java 21) to make the ordering contract explicit in the API. {@code SequencedSet} extends
 * {@link java.util.Set} with {@code getFirst()}/{@code getLast()} and reverse-order views.
 *
 * <p>Compare with {@code 1_hashset-refactored-record/SongsProcessor.java}: the only
 * structural change is {@code HashSet} → {@code LinkedHashSet} and the return types of
 * methods that expose the collection directly.
 */
public class SongsProcessor {

    private static final int EXPECTED_FIELDS = 4;

    private final SequencedSet<Song> songs;

    private SongsProcessor(SequencedSet<Song> songs) {
        this.songs = songs;
    }

    /**
     * Creates a {@code SongsProcessor} by reading and parsing a song file at the given path.
     *
     * <p>Songs are collected into a {@link LinkedHashSet}: duplicates are removed and the
     * remaining entries preserve the order of their first occurrence in the file.
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

            // LinkedHashSet: same deduplication as HashSet, but insertion order is preserved.
            // The first occurrence of each song wins; subsequent duplicates are silently dropped.
            SequencedSet<Song> songs = new LinkedHashSet<>(parsed);

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
     * Returns all unique songs in the order they first appeared in the file.
     *
     * <p>The returned {@link SequencedSet} is unmodifiable. Its {@code getFirst()} and
     * {@code getLast()} methods return the first and last songs encountered in the file,
     * after deduplication.
     */
    public SequencedSet<Song> allSongs() {
        return java.util.Collections.unmodifiableSequencedSet(songs);
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
     * Results are returned in insertion order.
     *
     * @param name the artist name fragment to search for (case-insensitive)
     * @return an immutable list of matching songs in insertion order
     */
    public List<Song> findByArtist(String name) {
        var lower = name.toLowerCase();
        return songs.stream()
                .filter(s -> s.artist().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Finds all songs released in the given year.
     * Results are returned in insertion order.
     *
     * @param year the release year to filter by
     * @return an immutable list of matching songs in insertion order
     */
    public List<Song> findByYear(Year year) {
        return songs.stream()
                .filter(s -> s.year().equals(year))
                .toList();
    }

    /**
     * Returns all songs from the earliest year in the data set, in insertion order.
     *
     * <p>The result is a {@link SequencedSet} backed by a {@link LinkedHashSet}, so iteration
     * order matches the order of first appearance in the file.
     */
    public SequencedSet<Song> findOldestSongs() {
        var oldest = songs.stream()
                .min(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.year().equals(oldest.year()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns all songs from the most recent year in the data set, in insertion order.
     */
    public SequencedSet<Song> findMostRecentSongs() {
        var recent = songs.stream()
                .max(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.year().equals(recent.year()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Groups all songs by artist name. Within each group, songs appear in insertion order.
     *
     * <p>{@code Collectors.groupingBy()} is the stream equivalent of SQL's {@code GROUP BY}.
     */
    public Map<String, List<Song>> groupByArtist() {
        return songs.stream()
                .collect(Collectors.groupingBy(Song::artist));
    }

    /**
     * Returns the distinct set of artist names in the order of their first appearance in the
     * file — a direct consequence of iterating a {@link LinkedHashSet}.
     *
     * <p>Compare with {@code 1_hashset-refactored-record}: there, {@code getArtists()} returns
     * an unordered {@code Set}. Here the order is deterministic and meaningful.
     */
    public SequencedSet<String> getArtists() {
        return songs.stream()
                .map(Song::artist)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
