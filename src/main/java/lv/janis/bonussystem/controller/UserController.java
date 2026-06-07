package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.AppUserRepository;
import lv.janis.bonussystem.model.AppUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(AppUserRepository appUserRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String users(Model model, HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("users", appUserRepository.findAll());

        return "users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role,
                          HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        if (appUserRepository.existsByUsername(username)) {
            return "redirect:/users?exists=true";
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        appUserRepository.save(user);

        return "redirect:/users?created=true";
    }

    @PostMapping("/users/change-password/{id}")
    public String changePassword(@PathVariable Long id,
                                 @RequestParam String newPassword,
                                 HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        AppUser user = appUserRepository.findById(id).orElseThrow();

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);

        return "redirect:/users?passwordChanged=true";
    }

    @PostMapping("/users/change-role/{id}")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String role,
                             HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        AppUser user = appUserRepository.findById(id).orElseThrow();

        user.setRole(role);
        appUserRepository.save(user);

        return "redirect:/users?roleChanged=true";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Object loggedObject = session.getAttribute("loggedUser");

        if (loggedObject instanceof AppUser loggedUser) {
            if (loggedUser.getId() != null && loggedUser.getId().equals(id)) {
                return "redirect:/users?selfDelete=true";
            }
        }

        appUserRepository.deleteById(id);

        return "redirect:/users?deleted=true";
    }

    private boolean isAdmin(HttpSession session) {
        Object loggedObject = session.getAttribute("loggedUser");

        if (!(loggedObject instanceof AppUser user)) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}