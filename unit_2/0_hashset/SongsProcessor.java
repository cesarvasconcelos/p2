import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads songs from a semicolon-delimited text file into a {@link HashSet} and
 * exposes basic query operations.
 *
 * <p>Each line in the file must follow the format:
 * <pre>
 *   code;title;artist;year
 * </pre>
 *
 * <p>The {@link HashSet} removes duplicate entries automatically — but only if
 * {@link Song#equals} and {@link Song#hashCode} are correctly implemented.
 * Open {@code Song.java} to see the manual boilerplate that makes this work.
 */
public class SongsProcessor {

    private static final int EXPECTED_FIELDS = 4;

    private final Set<Song> songs;

    public SongsProcessor(String filePath) {
        Objects.requireNonNull(filePath, "filePath cannot be null");
        this.songs = buildSongsSet(loadLines(filePath));
    }

    private Set<Song> buildSongsSet(List<String> lines) {
        return lines.stream()
                .filter(line -> !line.isBlank())
                .map(this::parseLine)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<String> loadLines(String filePath) {
        try {
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: " + filePath, e);
        }
    }

    private Song parseLine(String line) {
        try {
            return createSong(splitDataFrom(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Malformed line (non-numeric code or year): " + line, e);
        }
    }

    private String[] splitDataFrom(String line) {
        // split(";", -1): the -1 limit tells split() to keep trailing empty strings.
        // Without it, a line like "1;Title;Artist;" would silently drop the last empty field.
        var parts = line.split(";", -1);
        if (parts.length < EXPECTED_FIELDS) {
            throw new IllegalArgumentException("Malformed line (expected 4 fields): " + line);
        }
        return parts;
    }

    private Song createSong(String[] fields) {
        var code   = Integer.parseInt(fields[0].trim());
        var title  = fields[1].trim();
        var artist = fields[2].trim();
        var year   = Year.of(Integer.parseInt(fields[3].trim()));
        return new Song(code, title, artist, year);
    }

    public long countSongs() {
        return songs.size();
    }

    public long countArtists() {
        return songs.stream()
                .map(Song::getArtist)
                .distinct()
                .count();
    }

    public Optional<Song> findByCode(int code) {
        return songs.stream()
                .filter(s -> s.getCode() == code)
                .findFirst();
    }

    public List<Song> findByArtist(String name) {
        var lower = name.toLowerCase();
        return songs.stream()
                .filter(s -> s.getArtist().toLowerCase().contains(lower))
                .toList();
    }

    public List<Song> findByYear(Year year) {
        return songs.stream()
                .filter(s -> s.getYear().equals(year))
                .toList();
    }

    public Set<Song> findOldestSongs() {
        Song oldest = songs.stream()
                .min(Comparator.comparing(Song::getYear))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.getYear().equals(oldest.getYear()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Song> findMostRecentSongs() {
        Song recent = songs.stream()
                .max(Comparator.comparing(Song::getYear))
                .orElseThrow(() -> new NoSuchElementException("No songs loaded"));
        return songs.stream()
                .filter(s -> s.getYear().equals(recent.getYear()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getArtists() {
        return songs.stream()
                .map(Song::getArtist)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Map<String, List<Song>> groupByArtist() {
        return songs.stream()
                .collect(Collectors.groupingBy(Song::getArtist));
    }
}
