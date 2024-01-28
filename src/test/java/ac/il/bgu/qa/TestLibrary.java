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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.is;

public class TestLibrary {

    @Mock
    DatabaseService databaseServiceMock = Mockito.mock(DatabaseService.class);
    @Mock
    ReviewService reviewServiceMock = Mockito.mock(ReviewService.class);

    private final PrintStream standardOut = System.out;
    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    @Mock
    Library library = new Library(databaseServiceMock, reviewServiceMock);

    @Mock
    NotificationService notificationServiceMock = Mockito.mock(NotificationService.class);


    Book book = Mockito.mock(Book.class);
    User user = Mockito.mock(User.class);
    @AfterEach
    void AfterEach() {
        Mockito.reset(databaseServiceMock, reviewServiceMock);
        Mockito.reset(book, user);
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
        Mockito.when(book.getISBN()).thenReturn(ISBN);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
    }

    @ParameterizedTest
    @ValueSource(strings ={""})
    @NullSource
    public void GivenBookHasNoTitle_WhenAddBook_ThenThrowsIllegalArgumentException(String title) {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn(title);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
    }

    @ParameterizedTest
    @ValueSource(strings={"","a3","3a","a3a","3","@","-aa-", "a--a", "a''a"})
    @NullSource
    public void GivenBookHasInvalidAuthor_WhenAddBook_ThenThrowsIllegalArgumentException(String author) {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn(author);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenAddBook_ThenBookIsAdded() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book with invalid borrowed state.");
    }

    @Test
    public void GivenBookIsAlreadyInLibrary_WhenAddBook_ThenThrowsIllegalArgumentException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(book.getAuthor()).thenReturn("AUTHOR");
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
    }
    @ParameterizedTest
    @CsvSource({"a.a,0000000000000","a-a,1122334455666","a'a,0000000000000","a a,1290000000004","aa,1290000000004","aa,1000000000009"})
    public void GivenBookIsGood_WhenAddBook_ThenAddBook(String author, String ISBN) {
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
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook("0000000000000", userID), "Invalid user Id.");
    }
    @Test
    public void GivenUserNotExist_WhenBorrowBook_ThenThrowsUserNotRegisteredException(){
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(null);
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.borrowBook("0000000000000", "111111111111"), "User not found!");
    }

    @Test
    public void GivenBookIsAlreadyBorrowed_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException(){
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById("111111111111")).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook("0000000000000", "111111111111"), "Book is already borrowed!");
    }

    @Test
    public void WhenBorrowBook_ThenBookIsBorrowed(){
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
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
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(null);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.returnBook("0000000000000"), "Book not found!");
    }

    @Test
    public void GivenBookIsNotAlreadyBorrowed_WhenReturnBook_ThenThrowsBookNotBorrowedException() {
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Assertions.assertThrows(BookNotBorrowedException.class, () -> library.returnBook("0000000000000"), "Book wasn't borrowed!");
    }

    @Test
    public void GivenAllGood_WhenReturnBook_ThenBookIsReturned(){
        Mockito.when(databaseServiceMock.getBookByISBN("0000000000000")).thenReturn(book);
        Mockito.when(book.isBorrowed()).thenReturn(true);
        library.returnBook("0000000000000");
        Mockito.verify(databaseServiceMock).returnBook("0000000000000");
        Mockito.verify(book).returnBook();
    }

    // Tests for: notifyUserWithBookReviews()
    @Test
    public void GivenISBNInvalid_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        Mockito.when(book.getISBN()).thenReturn(null);
        Mockito.when(user.getId()).thenReturn("111111111111");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Invalid ISBN.");
    }

    @Test
    public void GivenUserIdInvalidNull_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Invalid user Id.");
    }

    @Test
    public void GivenUserIdInvalidWithLetters_WhenNotifyUserWithBookReviews_ThenThrowsIllegalArgumentException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("123AbC111111");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Invalid user Id.");
    }

    @Test
    public void GivenBookIsNull_WhenNotifyUserWithBookReviews_ThenThrowsBookNotFoundException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(null);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Book not found!");
    }

   @Test
    public void GivenUserIsNull_WhenNotifyUserWithBookReviews_ThenThrowsUserNotFoundException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(null);
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "User not found!");
    }

    // here
    @Test
    public void GivenNullReviews_WhenNotifyUserWithBookReviews_ThenThrowsBookHasBadReviewsException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(null);
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No Reviews Found!");
    }

    @Test
    public void GivenEmptyReviews_WhenNotifyUserWithBookReviews_ThenThrowsBookHasBadReviewsException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<String>());
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No Reviews Found!");
    }


    @Test
    public void GivenFailedNotification_WhenNotifyUserWithBookReviews_ThenThrowsNotificationException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<>(Arrays.asList("Review 1", "Review 2")));
        Mockito.doThrow(new NotificationException("")).when(user).sendNotification("Reviews for '" + "TITLE" + "':\n" + String.join("\n", new ArrayList<>(Arrays.asList("Review 1", "Review 2"))));
        System.setErr(new PrintStream(outputStreamCaptor));
        Assertions.assertThrows(NotificationException.class,()->library.notifyUserWithBookReviews(book.getISBN(), user.getId()),"Notification failed!");
        Mockito.verify(user,Mockito.times(5)).sendNotification("Reviews for '" + "TITLE" + "':\n" + String.join("\n", new ArrayList<>(Arrays.asList("Review 1", "Review 2"))));
        Assertions.assertEquals("Notification failed! Retrying attempt 1/5\r\nNotification failed! Retrying attempt 2/5\r\nNotification failed! Retrying attempt 3/5\r\nNotification failed! Retrying attempt 4/5\r\nNotification failed! Retrying attempt 5/5", outputStreamCaptor.toString().trim());
        System.setErr(standardErr);
    }

    @Test
    public void GivenFailedNotification_WhenNotifyUserWithBookReviews_ThenThrowsNotificationFailedException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(null);
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No Reviews Found!");
    }

    @Test
    public void GivenReviewServiceUnavailable_WhenNotifyUserWithBookReviews_ThenThrowsReviewServiceUnavailableException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenThrow(new ReviewException(""));
        Assertions.assertThrows(ReviewServiceUnavailableException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Review service unavailable!");
    }

    @Test
    public void GivenFinally_WhenNotifyUserWithBookReviews_ThenThrowsNotificationFailedException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<>(Arrays.asList("Review 1", "Review 2")));
        library.notifyUserWithBookReviews(book.getISBN(), user.getId());
        Mockito.verify(reviewServiceMock).close();
    }

    // Tests for: getBookByISBN

    @ParameterizedTest
    @ValueSource(strings ={"1000000000000","0000000000001","00000000000a1","0---000000000001","00000000000000","000000000000"})
    @NullAndEmptySource
    public void GivenInvalidISBN_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String ISBN) {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(ISBN, user.getId()), "Invalid ISBN.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenInvalidUserId_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String userId) {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(book.getISBN(), userId), "Invalid user Id.");
    }

    @Test
    public void GivenNullUserId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(book.getISBN(), null), "Invalid user Id.");
    }

    @Test
    public void GivenBookIsNull_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(null);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Book not found!");
    }

    @Test
    public void GivenBookIsBorrowed_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(user.getId()).thenReturn("111111111111");;
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Book was already borrowed!");
    }

    @Test
    public void NotifyUserFailed_WhenGetBookByISBN_ThenPrintsNotificationFailed() {
        Mockito.when(book.getISBN()).thenReturn("0000000000000");
        Mockito.when(book.getTitle()).thenReturn("TITLE");
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(databaseServiceMock.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Mockito.when(reviewServiceMock.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<>(Arrays.asList("Review 1", "Review 2")));
        Mockito.doThrow(new NotificationException("")).when(user).sendNotification("Reviews for '" + "TITLE" + "':\n" + String.join("\n", new ArrayList<>(Arrays.asList("Review 1", "Review 2"))));
        System.setOut(new PrintStream(outputStreamCaptor));
        Assertions.assertEquals(library.getBookByISBN(book.getISBN(), user.getId()), book);
        Assertions.assertEquals("Notification failed!", outputStreamCaptor.toString().trim());
        System.setOut(standardOut);
    }


    // Tests for: registerUser

    @Test
    public void GivenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(null), "Invalid user.");
    }

    @Test
    public void GivenNullUserId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Mockito.when(user.getId()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
    }

    @Test
    public void GivenNullUserName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(user.getName()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user name.");
    }

    @Test
    public void GivenNullNotificationService_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(user.getName()).thenReturn("Test User");
        Mockito.when(user.getNotificationService()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid notification service.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"","a111111111111", "11111111111","1111111111111","aaaaaaaaaaaa","            "})
    public void GivenInvalidUserId_WhenRegisterUser_ThenThrowIllegalArgumentException(String userId) {
        Mockito.when(user.getId()).thenReturn(userId);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
    }

    @Test
    public void GivenInvalidUserName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(user.getName()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user name.");    }

    @Test
    public void GivenUserAlreadyExists_WhenRegisterUser_ThenThrowUserAlreadyExistsException() {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(user.getName()).thenReturn("Test User");
        Mockito.when(user.getNotificationService()).thenReturn(notificationServiceMock);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "User already exists.");
    }

    @Test
    public void GivenValidUser_WhenRegisterUser_ThenRegisterUser() {
        Mockito.when(user.getId()).thenReturn("111111111111");
        Mockito.when(user.getName()).thenReturn("Test User");
        Mockito.when(user.getNotificationService()).thenReturn(notificationServiceMock);
        Mockito.when(databaseServiceMock.getUserById(user.getId())).thenReturn(null);
        library.registerUser(user);
        Mockito.verify(databaseServiceMock).registerUser(user.getId(), user);
    }
}

