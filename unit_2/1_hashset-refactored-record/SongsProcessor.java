import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads songs from a semicolon-delimited text file into a {@link HashSet} and exposes
 * query operations over them.
 *
 * <p>Each line in the source file must follow the format:
 * <pre>
 *   code;title;artist;year
 * </pre>
 *
 * <p>Instances are created exclusively through the static factory methods {@link #from(Path)}
 * and {@link #from(String)} — the constructor is private by design. This pattern (known as the
 * <em>static factory method</em> pattern) lets the class control and name its own construction
 * logic, making call sites easier to read:
 * <pre>{@code
 * var processor = SongsProcessor.from("worst-songs.txt");
 * }</pre>
 *
 * <p>Duplicate lines in the file are removed automatically by the {@link HashSet}. This works
 * because {@link Song} is a {@code record}: the compiler generates {@code equals} and
 * {@code hashCode} from all fields, so two songs with identical data are treated as the same
 * entry — with zero manual code in {@code Song.java}.
 *
 * <p>Compare with {@code 0_hashset/SongsProcessor.java}, which uses a public constructor
 * and a plain {@code Song} class that requires manual {@code equals}/{@code hashCode}.
 */
public class SongsProcessor {

    private static final int EXPECTED_FIELDS = 4;

    private final Set<Song> songs;

    private SongsProcessor(Set<Song> songs) {
        this.songs = songs;
    }

    /**
     * Creates a {@code SongsProcessor} by reading and parsing a song file at the given path.
     *
     * <p>The method reads the file line by line, parses each line into a {@link Song}, and
     * collects them into a {@link HashSet}. Duplicate entries (identical lines in the file)
     * are removed automatically because {@code Song} is a record with structural equality.
     *
     * @param path the path to the song data file
     * @return a fully initialised {@code SongsProcessor}
     * @throws UncheckedIOException if the file cannot be opened or read
     */
    public static SongsProcessor from(Path path) {
        try {
            // Files.lines() opens the file and returns a lazy Stream<String>.
            // The stream is consumed immediately by .toList(), which also closes it.
            var parsed = Files.lines(path)
                    .filter(line -> !line.isBlank())
                    .map(SongsProcessor::parseLine)
                    .toList();

            // HashSet removes duplicate Song entries.
            // Song is a record, so equals/hashCode compare all fields automatically —
            // no manual override needed. Two lines with identical data = one entry in the set.
            Set<Song> songs = new HashSet<>(parsed);

            return new SongsProcessor(songs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    /**
     * Parses a single semicolon-delimited line into a {@link Song}.
     *
     * <p>Expected format: {@code code;title;artist;year}
     *
     * <p>This method is {@code private static} because it does not depend on any instance
     * state — it is a pure transformation from one line of text to a {@code Song}.
     */
    private static Song parseLine(String line) {
        try {
            return createSong(splitDataFrom(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Malformed line (non-numeric code or year): " + line, e);
        }
    }

    private static String[] splitDataFrom(String line) {
        // split(";", -1): the -1 limit tells split() to keep trailing empty strings.
        // Without it, a line like "1;Title;Artist;" would silently drop the last empty field.
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
     * Finds the song with the given code.
     *
     * <p>Returns an {@link Optional} rather than {@code null} to make the "not found" case
     * explicit in the method signature. Callers are forced to handle both possibilities:
     * <pre>{@code
     * processor.findByCode(1258)
     *          .ifPresent(song -> IO.println(song.title()));
     * }</pre>
     */
    public Optional<Song> findByCode(int code) {
        return songs.stream()
                .filter(s -> s.code() == code)
                .findFirst();
    }

    /**
     * Finds all songs whose artist name contains the given search term, ignoring letter case.
     *
     * @param name the artist name fragment to search for (case-insensitive)
     * @return an immutable list of matching songs
     */
    public List<Song> findByArtist(String name) {
        var lower = name.toLowerCase();
        return songs.stream()
                .filter(s -> s.artist().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Finds all songs released in the given year.
     *
     * @param year the release year to filter by
     * @return an immutable list of matching songs
     */
    public List<Song> findByYear(Year year) {
        return songs.stream()
                .filter(s -> s.year().equals(year))
                .toList();
    }

    /** Returns all songs from the earliest year in the data set. */
    public Set<Song> findOldestSongs() {
        var oldest = songs.stream()
                .min(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.year().equals(oldest.year()))
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Returns all songs from the most recent year in the data set. */
    public Set<Song> findMostRecentSongs() {
        var recent = songs.stream()
                .max(Comparator.comparing(Song::year))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.year().equals(recent.year()))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Groups all songs by artist name.
     *
     * <p>{@code Collectors.groupingBy()} is the stream equivalent of SQL's {@code GROUP BY}.
     */
    public Map<String, List<Song>> groupByArtist() {
        return songs.stream()
                .collect(Collectors.groupingBy(Song::artist));
    }

    /** Returns the distinct set of artist names across all songs. */
    public Set<String> getArtists() {
        return songs.stream()
                .map(Song::artist)
                .collect(Collectors.toUnmodifiableSet());
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
