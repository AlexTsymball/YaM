package project.com.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.com.Entity.Book;
import project.com.Entity.User;
import project.com.Service.UserService;
import project.com.Service.BookService;

import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * The UserController class for control user.
 * @autor STS
 * @version 1.1
 */
@Controller
public class UserController {


    @Autowired
    private UserService userService;

    /**
     * The userProfile() method return user's page.
     * @param id;
     * @param model;
     * @return user.html
     */
    @RequestMapping(value = "/user", method = GET)
    public String userProfile(@RequestParam("id") Long id, Model model){
        User user = userService.findById(id).orElse(null);
        model.addAttribute("user",user);
        return "user";
    }

    /**
     * The profile() method return user's page where you can see all books that this user download.
     * @param model;
     * @return profile.page
     */
    @RequestMapping(value = "/profile", method = GET)
    public String profile(Model model){
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";
        List<Book> books = user.getBooks();
        model.addAttribute("user",user);
        model.addAttribute("allBooks",books);
        return "profile";
    }
}
