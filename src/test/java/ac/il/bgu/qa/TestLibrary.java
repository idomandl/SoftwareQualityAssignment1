package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

public class TestLibrary {

    @Mock
    DatabaseService databaseServiceMock = Mockito.mock(DatabaseService.class);
    @Mock
    ReviewService reviewServiceMock = Mockito.mock(ReviewService.class);

    @Mock
    Library library = new Library(databaseServiceMock, reviewServiceMock);

    @Mock
    NotificationService notificationServiceMock = Mockito.mock(NotificationService.class);

    @BeforeAll
    static void beforeAll() {
        System.out.println("Before all tests");
    }

    @Test
    public void GivenBookIsNull_WhenAddBook_ThenThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
    }

    @ParameterizedTest
    @ValueSource(strings ={"1000000000000","0000000000001","00000000000a1","0---000000000001","00000000000000","000000000000",
            "1293100000005","1290000000003","1290000000002","1290000000001","1290000000000","1290000000006","1290000000007",
            "1290000000008","1290000000009","1290000000014"})
    @NullAndEmptySource
    public void GivenBookHasInvalidISBN_WhenAddBook_ThenThrowsIllegalArgumentException(String ISBN) {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn(ISBN);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
    }

    @ParameterizedTest
    @ValueSource(strings ={""})
    @NullSource
    public void GivenBookHasNoTitle_WhenAddBook_ThenThrowsIllegalArgumentException(String title) {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn(title);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
    }

    @ParameterizedTest
    @ValueSource(strings={"","a3","3a","a3a","3","@","-aa-", "a--a", "a''a"})
    @NullSource
    public void GivenBookHasInvalidAuthor_WhenAddBook_ThenThrowsIllegalArgumentException(String author) {
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn(author);
            Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenAddBook_ThenBookIsAdded() {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book with invalid borrowed state.");
    }

    @Test
    public void GivenBookIsAlreadyInLibrary_WhenAddBook_ThenThrowsIllegalArgumentException() {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
    }
    @ParameterizedTest
    @CsvSource({"a.a,0000000000000","a-a,1122334455666","a'a,0000000000000","a a,1290000000004","aa,1290000000004","aa,1000000000009"})
    public void GivenBookIsGood_WhenAddBook_ThenAddBook(String author, String ISBN) {
        Book book = Mockito.mock(Book.class);
        Mockito.when(book.getISBN()).thenReturn(ISBN);
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn(author);
        Mockito.when(databaseServiceMock.getBookByISBN(ISBN)).thenReturn(null);
        library.addBook(book);
        Mockito.verify(databaseServiceMock).addBook(ISBN, book);
    }

    @ParameterizedTest
    @CsvSource({",1","1000000000000,1","0000000000001,1","00000000000a1,1","0---000000000001,1","00000000000000,1","000000000000,1","1000000000001,1"})
    public void GivenInvalidBookISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException(String ISBN, String userID) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(ISBN, userID), "Invalid ISBN.");
    }

    @ParameterizedTest
    @NullSource
    public void GivenBookIsNotInLibrary_WhenBorrowBook_ThenThrowsBookNotFoundException(Book book) {
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.borrowBook("0000000000000", "1"), "Book not found!");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenUserIdNotValid_WhenBorrowBook_ThenThrowsIllegalArgumentException(String userID) {
        Book book = Mockito.mock(Book.class);
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook("0000000000000", userID), "Invalid user Id.");
    }
    @Test
    public void GivenUserNotExist_WhenBorrowBook_ThenThrowsUserNotRegisteredException(){
        Book book = Mockito.mock(Book.class);
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(null);
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.borrowBook("0000000000000", "111111111111"), "User not found!");
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException(){
        Book book = Mockito.mock(Book.class);
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        User user = Mockito.mock(User.class);
        Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook("0000000000000", "111111111111"), "Book is already borrowed!");
    }

    @Test
    public void WhenBorrowBook_ThenBookIsBorrowed(){
        Book book = Mockito.mock(Book.class);
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        User user = Mockito.mock(User.class);
        Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        library.borrowBook("0000000000000", "111111111111");
        Mockito.verify(databaseServiceMock).borrowBook("0000000000000", "111111111111");
        Mockito.verify(book).borrow();
    }
    @ParameterizedTest
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenISBNInvalid_WhenReturnBook_ThenThrowsIllegalArgumentException(String ISBN) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.returnBook(ISBN), "Invalid ISBN.");
    }

    @Test
    public void GivenBookNotFoundForISBN_WhenReturnBook_ThenThrowsBookNotFoundException() {
        try{
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(null);
            library.returnBook("0000000000000");
            Assertions.fail("Expected an BookNotFoundException to be thrown");
        } catch (BookNotFoundException e) {
            Assertions.assertEquals("Book not found!", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsNotAlreadyBorrowed_WhenReturnBook_ThenThrowsBookNotBorrowedException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            Mockito.when(book.isBorrowed()).thenReturn(false);
            library.returnBook("0000000000000");
            Assertions.fail("Expected an BookNotBorrowedException to be thrown");
        } catch (BookNotBorrowedException e) {
            Assertions.assertEquals("Book wasn't borrowed!", e.getMessage());
        }
    }

    @Test
    public void GivenAllGood_WhenReturnBook_ThenBookIsReturned(){
        Book book = Mockito.mock(Book.class);
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(book.isBorrowed()).thenReturn(true);
        library.returnBook("0000000000000");
        Mockito.verify(databaseServiceMock).returnBook("0000000000000");
        Mockito.verify(book).returnBook();
    }

    // Tests for: notifyUserWithBookReviews()
    @Test
    public void GivenISBNInvalid_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn(null);
            Mockito.when(user.getId()).thenReturn("111111111111");
            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
    }

    @Test
    public void GivenUserIdInvalidNull_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn(null);
            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenUserIdInvalidWithLetters_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("123AbC111111");
            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsNull_WhenNotifyUserWithBookReviews_ThenThrowsBookNotFoundException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(null);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());

            Assertions.fail("Expected an BookNotFoundException to be thrown");

        } catch (BookNotFoundException e) {
            Assertions.assertEquals("Book not found!", e.getMessage());
        }
    }

//    @Test
//    public void GivenUserIsNull_WhenNotifyUserWithBookReviews_ThenThrowsUserNotFoundException() {
//        try{
//            Book book = Mockito.mock(Book.class);
//            User user = Mockito.mock(User.class);
//
//            Mockito.when(book.getISBN()).thenReturn("0000000000000");
//            Mockito.when(user.getId()).thenReturn("111111111111");
//
//            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
//            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(null);
//
//            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
//
//            Assertions.fail("Expected an UserNotRegisteredException to be thrown");
//
//        } catch (BookNotFoundException e) {
//            Assertions.assertEquals("User not found!", e.getMessage());
//        }
//    }

//    @Test
//    public void GivenBookIsBorrowed_WhenNotifyUserWithBookReviews_ThenThrowsBookAlreadyBorrowedException() {
//        try{
//            Book book = Mockito.mock(Book.class);
//            User user = Mockito.mock(User.class);
//
//            Mockito.when(book.getISBN()).thenReturn("0000000000000");
//            Mockito.when(user.getId()).thenReturn("111111111111");
//
//            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
//            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
//
//            Mockito.when(book.isBorrowed()).thenReturn(true);
//
//            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
//            Assertions.fail("Expected an BookAlreadyBorrowedException to be thrown");
//
//        } catch (BookAlreadyBorrowedException e) {
//            Assertions.assertEquals("Book already borrowed!", e.getMessage());
//        }
//    }

    @Test
    public void GivenNullReviews_WhenNotifyUserWithBookReviews_ThenThrowsBookHasBadReviewsException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            Mockito.when(book.isBorrowed()).thenReturn(false);

            Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(null);

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an NoReviewsFoundException to be thrown");

        } catch (NoReviewsFoundException e) {
            Assertions.assertEquals("No reviews found!", e.getMessage());
        }
    }

    @Test
    public void GivenEmptyReviews_WhenNotifyUserWithBookReviews_ThenThrowsBookHasBadReviewsException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            Mockito.when(book.isBorrowed()).thenReturn(false);

            Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<String>());

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an NoReviewsFoundException to be thrown");

        } catch (NoReviewsFoundException e) {
            Assertions.assertEquals("No reviews found!", e.getMessage());
        }
    }

    @Test
    public void GivenFailedNotification_WhenNotifyUserWithBookReviews_ThenThrowsNotificationFailedException() {
        //try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            Mockito.when(book.isBorrowed()).thenReturn(false);

            Mockito.when(user.getNotificationService()).thenReturn(notificationServiceMock);
            Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<String>(){{add("Review 1");}});

            /* Todo:

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an NotificationFailedException to be thrown");

        } catch (NotificationException e) {
            Assertions.assertEquals("Notification failed!", e.getMessage()); */
    }

    // Tests for: getBookByISBN

    @ParameterizedTest
    @ValueSource(strings ={"1000000000000","0000000000001","00000000000a1","0---000000000001","00000000000000","000000000000"})
    @NullAndEmptySource
    public void GivenInvalidISBN_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String ISBN) {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(user.getId()).thenReturn("111111111111");
            //library.addBook(book);
            library.getBookByISBN(ISBN, user.getId());
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenInvalidUserId_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String userId) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");

            library.getBookByISBN(book.getISBN(), userId);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenNullUserId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");

            library.getBookByISBN(book.getISBN(), null);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsNull_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(null);

            library.getBookByISBN(book.getISBN(), user.getId());
            Assertions.fail("Expected an BookNotFoundException to be thrown");

        } catch (BookNotFoundException e) {
            Assertions.assertEquals("Book not found!", e.getMessage());
        }
    }

    // Tests for: registerUser

    @Test
    public void GivenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        try{
            library.registerUser(null);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user.", e.getMessage());
        }
    }

    @Test
    public void GivenNullUserId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn(null);

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenNullUserName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn("111111111111");
            Mockito.when(user.getName()).thenReturn(null);

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user name.", e.getMessage());
        }
    }

//    @Test
//    public void GivenNullNotificationService_WhenRegisterUser_ThenThrowIllegalArgumentException() {
//        try{
//            User user = Mockito.mock(User.class);
//            Mockito.when(user.getId()).thenReturn("111111111111");
//            Mockito.when(user.getName()).thenReturn("Test User");
//            Mockito.when(user.getNotificationService()).thenReturn(null);
//
//            library.registerUser(user);
//            Assertions.fail("Expected an IllegalArgumentException to be thrown");
//
//        } catch (IllegalArgumentException e) {
//            Assertions.assertEquals("Invalid user address.", e.getMessage());
//        }
//    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenInvalidUserId_WhenRegisterUser_ThenThrowIllegalArgumentException(String userId) {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn(userId);

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }

    @Test
    public void GivenInvalidUserName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn("111111111111");
            Mockito.when(user.getName()).thenReturn("");

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user name.", e.getMessage());
        }
    }

    @Test
    public void GivenUserAlreadyExists_WhenRegisterUser_ThenThrowUserAlreadyExistsException() {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn("111111111111");
            Mockito.when(user.getName()).thenReturn("Test User");
            Mockito.when(user.getNotificationService()).thenReturn(notificationServiceMock);

            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("User already exists.", e.getMessage());
        }
    }
}

