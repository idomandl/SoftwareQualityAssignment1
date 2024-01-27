package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.*;
import com.sun.tools.javac.comp.Todo;
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
        try{
            library.addBook(null);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid book.", e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings ={"1000000000000","0000000000001","00000000000a1","0---000000000001","00000000000000","000000000000"})
    @NullAndEmptySource
    public void GivenBookHasInvalidISBN_WhenAddBook_ThenThrowsIllegalArgumentException(String ISBN) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn(ISBN);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings ={""})
    @NullSource
    public void GivenBookHasNoTitle_WhenAddBook_ThenThrowsIllegalArgumentException(String title) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn(title);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid title.", e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings={"","a3","3a","a3a","3","@","-aa-", "a--a", "a''a"})
    @NullSource
    public void GivenBookHasInvalidAuthor_WhenAddBook_ThenThrowsIllegalArgumentException(String author) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn(author);
            library.addBook(book);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid author.", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenAddBook_ThenBookIsAdded() {
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
    @ParameterizedTest
    @ValueSource(strings={"a.a","a-a","a'a","a a","aa"})
    public void GivenBookIsGood_WhenAddBook_ThenAddBook(String author) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(book.getTitle()).thenReturn("TITLE");
            Mockito.when(book.getAuthor()).thenReturn(author);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(null);
            Answer<Void> answer = new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
                    return null;
                }
            };
            Mockito.when(databaseServiceMock.addBook("0000000000000", book)).thenAnswer(answer);
            library.addBook(book);
            Assertions.assertEquals(databaseServiceMock.getBookByISBN("0000000000000"),book);
        } catch (IllegalArgumentException e) {
            Assertions.fail("no exception should be thrown");
        }
    }

    @ParameterizedTest
    @CsvSource({",1","1000000000000,1","0000000000001,1","00000000000a1,1","0---000000000001,1","00000000000000,1","000000000000,1"})
    public void GivenInvalidBookISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException(String ISBN, String userID) {
        try{
            library.borrowBook(ISBN, userID);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
    }

    @ParameterizedTest
    @NullSource
    public void GivenBookIsNotInLibrary_WhenBorrowBook_ThenThrowsBookNotFoundException(Book book) {
        try{
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            library.borrowBook("0000000000000", "1");
            Assertions.fail("Expected an BookNotFoundException to be thrown");
        } catch (BookNotFoundException e) {
            Assertions.assertEquals("Book not found!", e.getMessage());
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenUserIdNotValid_WhenBorrowBook_ThenThrowsIllegalArgumentException(String userID) {
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            library.borrowBook("0000000000000", userID);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user Id.", e.getMessage());
        }
    }
    @Test
    public void GivenUserNotExist_WhenBorrowBook_ThenThrowsUserNotRegisteredException(){
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(null);
            library.borrowBook("0000000000000", "111111111111");
            Assertions.fail("Expected an UserNotRegisteredException to be thrown");
        } catch (UserNotRegisteredException e) {
            Assertions.assertEquals("User not found!", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException(){
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            User user = Mockito.mock(User.class);
            Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(user);
            Mockito.when(book.isBorrowed()).thenReturn(true);
            library.borrowBook("0000000000000", "111111111111");
            Assertions.fail("Expected an BookAlreadyBorrowedException to be thrown");
        } catch (BookAlreadyBorrowedException e) {
            Assertions.assertEquals("Book is already borrowed!", e.getMessage());
        }
    }

    @Test
    public void WhenBorrowBook_ThenBookIsBorrowed(){
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            User user = Mockito.mock(User.class);
            Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(user);
            Mockito.when(book.isBorrowed()).thenReturn(false);
            Answer<Void> answer = new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    Mockito.when(book.isBorrowed()).thenReturn(true);
                    return null;
                }
            };
            Mockito.doAnswer(answer).when(book).borrow();
            library.borrowBook("0000000000000", "111111111111");
            Assertions.assertTrue(book.isBorrowed());
        } catch (BookAlreadyBorrowedException e) {
            Assertions.fail("no exception should be thrown");
        }
    }
    @ParameterizedTest
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenISBNInvalid_WhenReturnBook_ThenThrowsIllegalArgumentException(String ISBN) {
        try{
            library.returnBook(ISBN);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid ISBN.", e.getMessage());
        }
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
        try{
            Book book = Mockito.mock(Book.class);
            Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
            Mockito.when(book.isBorrowed()).thenReturn(true);
            Answer<Void> answer = new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    Mockito.when(book.isBorrowed()).thenReturn(false);
                    return null;
                }
            };
            Mockito.doAnswer(answer).when(book).returnBook();
            library.returnBook("0000000000000");
            Assertions.assertFalse(book.isBorrowed());
        } catch (BookNotBorrowedException e) {
            Assertions.fail("no exception should be thrown");
        }
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

    @Test
    public void GivenUserIsNull_WhenNotifyUserWithBookReviews_ThenThrowsUserNotFoundException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(null);

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());

            Assertions.fail("Expected an UserNotRegisteredException to be thrown");

        } catch (BookNotFoundException e) {
            Assertions.assertEquals("User not found!", e.getMessage());
        }
    }

    @Test
    public void GivenBookIsBorrowed_WhenNotifyUserWithBookReviews_ThenThrowsBookAlreadyBorrowedException() {
        try{
            Book book = Mockito.mock(Book.class);
            User user = Mockito.mock(User.class);

            Mockito.when(book.getISBN()).thenReturn("0000000000000");
            Mockito.when(user.getId()).thenReturn("111111111111");

            Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
            Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);

            Mockito.when(book.isBorrowed()).thenReturn(true);

            library.notifyUserWithBookReviews(book.getISBN(), user.getId());
            Assertions.fail("Expected an BookAlreadyBorrowedException to be thrown");

        } catch (BookAlreadyBorrowedException e) {
            Assertions.assertEquals("Book already borrowed!", e.getMessage());
        }
    }

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
            Assertions.assertEquals("Invalid user ID.", e.getMessage());
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
            Assertions.assertEquals("Invalid user ID.", e.getMessage());
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
            Assertions.assertEquals("Book not found.", e.getMessage());
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
            Assertions.assertEquals("Invalid user ID.", e.getMessage());
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

    @Test
    public void GivenNullNotificationService_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        try{
            User user = Mockito.mock(User.class);
            Mockito.when(user.getId()).thenReturn("111111111111");
            Mockito.when(user.getName()).thenReturn("Test User");
            Mockito.when(user.getNotificationService()).thenReturn(null);

            library.registerUser(user);
            Assertions.fail("Expected an IllegalArgumentException to be thrown");

        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Invalid user address.", e.getMessage());
        }
    }

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
            Assertions.assertEquals("Invalid user ID.", e.getMessage());
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

