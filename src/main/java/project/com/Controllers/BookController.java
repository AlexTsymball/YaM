package project.com.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.com.Entity.*;
import project.com.Service.BookService;
import project.com.Service.CommentService;
import project.com.Service.UserService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * The BookController class for control books.
 * @autor STS
 * @version 1.1
 */
@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    /**
     * The booAddForm() method returns bookform-page.
     * @param model;
     * @return bookAdd.html
     */
    @RequestMapping(value = "/bookAdd", method = RequestMethod.GET)
    public String bookAddForm(Model model) {
        List<Genre> genre = Arrays.asList(Genre.values());
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("genres", genre);
        model.addAttribute("book", new BookDto());
        return "bookAdd";
    }

    /**
     * The submit() method returns book-page after bookform-page and create book that you write in book-form.
     * @param bookDto;
     * @return bookById.html
     * @throws IOException
     */
    @RequestMapping(value = "/bookAdd", method = RequestMethod.POST)
    public String submit(@ModelAttribute("book") BookDto bookDto) throws IOException {
        String uuidFileName = UUID.randomUUID().toString();
        MultipartFile file = bookDto.getImage();
        String resultFileName = uuidFileName + "." + file.getOriginalFilename();
        file.transferTo(new File("E:\\project]\\YaM\\src\\main\\resources\\static\\images\\books\\" + resultFileName));

        String uuidFileNameBook = UUID.randomUUID().toString();
        MultipartFile fileBook = bookDto.getBook();
        String resultFileNameBook = uuidFileNameBook + "." + fileBook.getOriginalFilename();
        fileBook.transferTo(new File("E:\\project]\\YaM\\src\\main\\resources\\static\\images\\books\\" + resultFileNameBook));

        User user = userService.getCurrentUser();
        Book book = new Book(bookDto);
        book.setImage("../images/books/" + resultFileName);
        book.setBook("E:\\project]\\YaM\\src\\main\\resources\\static\\books\\" + resultFileNameBook);
        book.setDownloader(user);
        bookService.createBook(book);
        user.addBook(book);
        userService.updateUser(user);
        return "redirect:/bookById?id=" + book.getId();
    }


    /**
     * The submit() method returns book-page .
     * @param id;
     * @param model;
     * @return bookById.html
     */
    @RequestMapping(value = "/bookById/{id}", method = RequestMethod.GET)
    public String submit(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findById(id).orElse(new Book());

        List<Comment> comments = commentService.findComentsForThisBookSortByDate(book.getId());
        StringBuilder currentLineFile = new StringBuilder();
        model.addAttribute("line", currentLineFile);
        model.addAttribute("comments", comments);
        model.addAttribute("book", book);
        model.addAttribute("comment", new Comment());
        return "bookById";
    }


    /**
     * The submit() method returns book-page , sets up rating and comments.
     * @param id;
     * @param comment;
     * @param rating;
     * @param model;
     * @return bookById.html
     */
    @RequestMapping(value = "/bookById/{id}", method = RequestMethod.POST)
    public String submitComment(@PathVariable("id") Long id,
                                @RequestParam(name = "comment", required = false, defaultValue = "") String comment,
                                @RequestParam(name = "rating", required = false, defaultValue = "0")
                                        Integer rating, Model model) {
        Book book = bookService.findById(id).orElse(new Book());
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        if (!comment.isEmpty()) {
            setComment(book, comment, currentUser);
        }
        if (rating != 0) {
            setRating(book, rating);
        }

        List<Comment> comments = commentService.findComentsForThisBookSortByDate(book.getId());

        StringBuilder currentLineFile = new StringBuilder();
        Path path1 = Paths.get(book.getBook());
        try (BufferedReader readerFile1 = Files.newBufferedReader(path1, Charset.forName("ASCII"))) {
            int i = 0;

            while (i != 5) {

                currentLineFile.append(readerFile1.readLine());

                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        model.addAttribute("line", currentLineFile);
        model.addAttribute("comments", comments);
        model.addAttribute("book", book);
        return "redirect:/bookById?id=" + book.getId();
    }

    /**
     * The search() method find book.
     * @param model;
     * @param search;
     * @return allBooks.html
     */
    @RequestMapping(value = "/allbook/search", method = RequestMethod.GET)
    public String search(ModelMap model, String search) {
        List<Book> allBooks = bookService.findBySearch(search);
        List<Genre> genre = Arrays.asList(Genre.values());
        model.addAttribute("genres", genre);
        model.addAttribute("allBooks", allBooks);
        model.addAttribute("search", search);
        return "allBooks";
    }

    /**
     * The searchGenre() method find all book by genre.
     * @param model;
     * @param id;
     * @return allBooks.html
     */
    @RequestMapping(value = "/allbook/search/genre", method = RequestMethod.GET)
    public String searchGenre(ModelMap model, Integer id) {
        List<Genre> genre = Arrays.asList(Genre.values());
        List<Book> allBooks = bookService.findAllByGenre(Genre.values()[id-1]);
        model.addAttribute("allBooks", allBooks);
        model.addAttribute("genres", genre);
        return "allBooks";
    }

    /**
     * The searchAuthor() method find all book by author.
     * @param model;
     * @param author;
     * @return allBooks.html
     */
    @RequestMapping(value = "/allbook/search/author", method = RequestMethod.GET)
    public String searchAuthor(ModelMap model, String author) {
        List<Genre> genre = Arrays.asList(Genre.values());
        List<Book> allBooks = bookService.findAllByAuthor(author);
        model.addAttribute("allBooks", allBooks);
        model.addAttribute("genres", genre);
        return "allBooks";
    }


    /**
     * The allBook() method find all book.
     * @param model;
     * @return allBooks.html
     */
    @RequestMapping(value = "/allbook", method = RequestMethod.GET)
    public String allBook(ModelMap model) {
        List<Book> allBooks = bookService.findAllOrderByRating();
        List<Genre> genre = Arrays.asList(Genre.values());
        model.addAttribute("genres", genre);
        model.addAttribute("allBooks", allBooks);
        return "allBooks";
    }

    /**
     * The setRating() method calculates and sets up rating.
     * @param book;
     * @param rating;
     */
    private void setRating(Book book, int rating){
        float ratingOld = book.getRating();
        int count = book.getCountRating();
        float ratingNew = (ratingOld * count + rating) / (count + 1);
        book.setRating(ratingNew);
        book.setCountRating(count + 1);
        bookService.updateBook(book);
    }

    /**
     * The setComment() method sets up comment.
     * @param book;
     * @param comment;
     * @param currentUser;
     */
    private void setComment(Book book, String comment, User currentUser){
        Comment newComment = new Comment(comment);
        Date date = Date.valueOf(LocalDate.now());
        newComment.setDate(date);
        newComment.setBook(book);
        newComment.setUser(currentUser);
        commentService.createComment(newComment);
        book.addComments(newComment);
        bookService.updateBook(book);
        currentUser.addComments(newComment);
        userService.updateUser(currentUser);
    }
}