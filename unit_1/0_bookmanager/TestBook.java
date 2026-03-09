void main() {
    var bookManager = new BookManager("files/books-data.txt");
    bookManager.addBook(new Book(456, "Effective Java", "Joshua Block", "ebook", 23.99));
    bookManager.removeBookBy(128);
    var bookFound = bookManager.findBookBy(456);
    bookFound.ifPresent(IO::print);
}
