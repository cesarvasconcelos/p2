import java.time.Year;
import java.util.Objects;

/**
 * Represents a music track.
 *
 * <p>This is a traditional Java class with a constructor, getters, setters, and manually
 * written {@code equals}, {@code hashCode}, and {@code toString} methods.
 *
 * <p>Notice how much boilerplate is required just to make this class work correctly with
 * a {@link java.util.HashSet}: the {@code equals} and {@code hashCode} methods must be
 * written and maintained by hand, following the contract defined in
 * <em>Effective Java</em> (Bloch, Item 10: Obey the general contract when overriding equals).
 *
 * <p>Compare this class with {@code 1_hashset/Song.java}, where a single {@code record}
 * declaration replaces all of this code — with zero boilerplate.
 */
public class Song {

    private int code;
    private String title;
    private String artist;
    private Year year;

    public Song(int code, String title, String artist, Year year) {
        setCode(code);
        setTitle(title);
        setArtist(artist);
        setYear(year);
    }

    public int getCode()        { return code; }
    public String getTitle()    { return title; }
    public String getArtist()   { return artist; }
    public Year getYear()       { return year; }

    public void setCode(int code) {
        if (code <= 0) throw new IllegalArgumentException("Code must be greater than 0");
        this.code = code;
    }

    public void setTitle(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        if (title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        this.title = title;
    }

    public void setArtist(String artist) {
        Objects.requireNonNull(artist, "Artist cannot be null");
        if (artist.isBlank()) throw new IllegalArgumentException("Artist cannot be blank");
        this.artist = artist;
    }

    public void setYear(Year year) {
        Objects.requireNonNull(year, "Year cannot be null");
        this.year = year;
    }

    /**
     * Two songs are equal if and only if their {@code code} fields match.
     *
     * <p>This follows the contract from <em>Effective Java</em> (Bloch, Item 10):
     * <ol>
     *   <li>Identity check — short-circuit if same object reference.
     *   <li>Null check — return false immediately for null input.
     *   <li>Class check — use {@code getClass()} because no subclass should redefine equality.
     *   <li>Cast — safe after the class check.
     *   <li>Field comparison — compare only the identity field ({@code code}).
     * </ol>
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Song song = (Song) other;
        return code == song.code;
    }

    /**
     * Must be consistent with {@code equals}: songs that are equal (same code)
     * must return the same hash code so that {@link java.util.HashSet} places them
     * in the same bucket and detects the duplicate.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return """
               Song[code=%d, title='%s', artist='%s', year=%s]\
               """.formatted(code, title, artist, year);
    }
}
