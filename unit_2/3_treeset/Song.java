import java.time.Year;
import java.util.Comparator;

/**
 * Represents a music track.
 *
 * <p>This record carries no natural ordering — there is no single axis (code, title, artist,
 * year) that is universally "the right" way to sort songs. Therefore {@code Song} does
 * <strong>not</strong> implement {@link Comparable}. Instead, four named {@link Comparator}
 * constants are provided so callers can choose an ordering axis explicitly and compose axes
 * with {@link Comparator#thenComparing}.
 *
 * <p>The compiler-generated {@code equals} and {@code hashCode} cover all four fields,
 * making {@code Song} safe for use in any {@link java.util.Set} implementation.
 *
 * <h2>Comparator constants and consistency with equals</h2>
 * <p>When a {@link java.util.TreeSet} uses one of these constants as its ordering, only
 * {@link #BY_CODE} is safe as the <em>sole</em> comparator: because song codes are unique in
 * this data set, {@code BY_CODE.compare(a, b) == 0} implies {@code a.equals(b)}, which is
 * the <strong>consistent-with-equals</strong> contract required to avoid silent data loss.
 *
 * <p>If song codes were ever reused across distinct songs, a multi-field comparator
 * ({@code BY_CODE.thenComparing(Song::title).thenComparing(Song::artist)…}) would be needed
 * to restore that guarantee.
 *
 * @param code   a unique numeric identifier for the song
 * @param title  the title of the song
 * @param artist the name of the performing artist or band
 * @param year   the calendar year the song was released
 */
record Song(int code, String title, String artist, Year year) {

    /**
     * Orders songs by numeric code, ascending.
     *
     * <p>This is the only constant that is safe as the sole backing comparator for a
     * {@link java.util.TreeSet}, provided song codes are unique in the data set.
     * See the class-level Javadoc for the reasoning.
     */
    static final Comparator<Song> BY_CODE =
            Comparator.comparingInt(Song::code);

    /**
     * Orders songs alphabetically by title, then by code within the same title.
     * The {@code code} tiebreaker ensures this comparator is consistent with equals:
     * two songs with the same title but different codes (e.g., a remake) are never
     * treated as duplicates by a {@link java.util.TreeSet}.
     */
    static final Comparator<Song> BY_TITLE =
            Comparator.comparing(Song::title)
                      .thenComparingInt(Song::code);

    /**
     * Orders songs alphabetically by artist name, then by title within the same artist,
     * then by code as a final tiebreaker.
     * The {@code code} tiebreaker ensures this comparator is consistent with equals:
     * two songs from the same artist with the same title but different codes are never
     * treated as duplicates by a {@link java.util.TreeSet}.
     */
    static final Comparator<Song> BY_ARTIST =
            Comparator.comparing(Song::artist)
                      .thenComparing(Song::title)
                      .thenComparingInt(Song::code);

    /**
     * Orders songs chronologically by release year, then by code within the same year.
     * Suitable for timeline-oriented queries and range searches.
     */
    static final Comparator<Song> BY_YEAR =
            Comparator.comparing(Song::year).thenComparingInt(Song::code);
}
