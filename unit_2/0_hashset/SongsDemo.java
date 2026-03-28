/**
 * Demonstrates the "messy" approach to HashSet deduplication.
 *
 * <p>{@code Song} is a plain class with manually written {@code equals}/{@code hashCode}.
 * The {@link SongsProcessor} loads the file and uses a {@link java.util.HashSet} to
 * remove duplicates — which only works because those methods are correctly implemented.
 *
 * <p>After running this demo, open {@code Song.java} and count the lines needed just to
 * make equality work. Then compare with {@code 1_hashset/Song.java}.
 */
void main() {
    var processor = new SongsProcessor("worst-songs.txt");

    IO.println("Total unique songs : " + processor.countSongs());
    IO.println("Oldest songs       : " + processor.findOldestSongs());
    IO.println("Most recent songs  : " + processor.findMostRecentSongs());
    IO.println("Artists            : " + processor.getArtists());
    IO.println("Grouped by artist  : " + processor.groupByArtist());
}
