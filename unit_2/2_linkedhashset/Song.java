import java.time.Year;

/**
 * Represents a music track.
 *
 * <p>This is the same {@code record} used in {@code 1_hashset-refactored-record} — reproduced
 * here unchanged to keep each example self-contained.
 *
 * <p>The compiler-generated {@code equals} and {@code hashCode} enable correct deduplication
 * in any {@link java.util.Set} implementation, including {@link java.util.LinkedHashSet}, which
 * is the focus of this example.
 *
 * @param code   a unique numeric identifier for the song
 * @param title  the title of the song
 * @param artist the name of the performing artist or band
 * @param year   the calendar year the song was released
 */
record Song(int code, String title, String artist, Year year) {}
