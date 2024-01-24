package ac.il.bgu.qa;

import ac.il.bgu.qa.services.*;;
import org.junit.jupiter.api.*;
import org.mockito.*;

public class TestLibrary {

    @Mock
    DatabaseService databaseServiceMock = Mockito.mock(DatabaseService.class);
    @Mock
    ReviewService reviewServiceMock = Mockito.mock(ReviewService.class);

    Library library = new Library(databaseServiceMock, reviewServiceMock);

    @BeforeAll
    static void beforeAll() {
        System.out.println("Before all tests");
    }

    @Test
    public void GivenBookIsNull_WhenAddBook_ThenThrowsIllegalArgumentException() {
        try{
            library.addBook(null);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid book.", e.getMessage());
        }
    }

    @Test
    public void GivenBookHasInvalidISBN_WhenAddBook_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("1000000000000");
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
    }

    @Test
    public void GivenBookHasNoTitle_WhenAddBook_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn(null);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid title.", e.getMessage());
        }
    }

    @Test
    public void GivenBookHasInvalidAuthor_WhenAddBook_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn(null);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid author.", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsNotInLibrary_WhenAddBook_ThenBookIsAdded() {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
        Mockito.when(book.isBorrowed()).thenReturn(true);
        try{
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Book with invalid borrowed state.", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsAlreadyInLibrary_WhenAddBook_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Book already exists.", e.getMessage());
        }
    }
    @Test
    public void GivenBookIsGood_WhenAddBook_ThenAddBook() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(null);
            library.addBook(book);
            Assertions.assertTrue(true);
            Assertions.assertTrue(true);
        } catch (IllegalArgumentException e) {
            Assertions.fail("no exception should be thrown");
        }
    }

    @Test




}
