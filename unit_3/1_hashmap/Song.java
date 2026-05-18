import java.time.Year;

/**
 * Represents a music track.
 *
 * <p>This type is declared as a Java {@code record} — an immutable data carrier. The compiler
 * automatically generates:
 * <ul>
 *   <li>A public canonical constructor: {@code Song(int, String, String, Year)}
 *   <li>Public accessor methods: {@code code()}, {@code title()}, {@code artist()}, {@code year()}
 *   <li>Implementations of {@code equals()}, {@code hashCode()}, and {@code toString()}
 * </ul>
 *
 * <h2>HashMap context: keys vs. values</h2>
 * <p>In this example, {@code Song} is used as a <strong>value</strong> in the map:
 * <pre>
 *   Map&lt;Integer, Song&gt; index  →  code (key) → Song (value)
 * </pre>
 *
 * <p>The {@code Integer} code is the map <strong>key</strong>. {@code HashMap} uses the
 * key's {@code hashCode()} and {@code equals()} to locate the right bucket and compare
 * entries — not the value's. So unlike a {@link java.util.HashSet} use case where
 * {@code Song.hashCode()} drives deduplication, here {@code Integer.hashCode()} does.
 *
 * <p>{@code Song.equals()} and {@code Song.hashCode()} still matter wherever songs are
 * used inside collections that <em>are</em> the values — for example, in the
 * {@code Map<String, Set<Song>>} returned by
 * {@link SongsProcessor#groupByArtist()}, songs are stored in a {@link java.util.Set},
 * so the record's generated equality is what prevents duplicates there.
 *
 * @param code   a unique numeric identifier for the song
 * @param title  the title of the song
 * @param artist the name of the performing artist or band
 * @param year   the calendar year the song was released
 */
record Song(int code, String title, String artist, Year year) {}
