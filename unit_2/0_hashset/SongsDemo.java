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
    IO.println("Total artists      : " + processor.countArtists());
    IO.println("Oldest songs       : " + processor.findOldestSongs());
    IO.println("Most recent songs  : " + processor.findMostRecentSongs());
    IO.println("Find by code 1258  : " + processor.findByCode(1258));
    IO.println("Find by artist ABBA: " + processor.findByArtist("ABBA"));
    IO.println("Find by year 1977  : " + processor.findByYear(java.time.Year.of(1977)));
    IO.println("Artists            : " + processor.getArtists());
    IO.println("Grouped by artist  : " + processor.groupByArtist());
}
