import java.time.Year;

/**
 * Represents a music track.
 *
 * <p>This type is declared as a Java {@code record} (introduced in Java 16), which means it is an
 * immutable data carrier. The compiler automatically generates:
 *
 * <ul>
 *   <li>A public canonical constructor: {@code Song(int, String, String, Year)}
 *   <li>Public accessor methods for each component: {@code code()}, {@code title()},
 *       {@code artist()}, {@code year()}
 *   <li>Implementations of {@code equals()}, {@code hashCode()}, and {@code toString()}
 * </ul>
 *
 * <p>Because {@code equals} and {@code hashCode} are generated automatically from all fields,
 * this record works correctly with a {@link java.util.HashSet} out of the box — with zero
 * manual boilerplate.
 *
 * <p>Compare with {@code 0_hashset/Song.java}: a plain class that requires a constructor,
 * getters, setters, and manually written {@code equals}/{@code hashCode}/{@code toString}
 * to achieve the same result.
 *
 * @param code   a unique numeric identifier for the song
 * @param title  the title of the song
 * @param artist the name of the performing artist or band
 * @param year   the calendar year the song was released; uses {@link java.time.Year} instead of a
 *               plain {@code int} to make the intent explicit
 */
record Song(int code, String title, String artist, Year year) {}
