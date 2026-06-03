package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.AppUserRepository;
import lv.janis.bonussystem.model.AppUser;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final AppUserRepository appUserRepository;

    public LoginController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {

        AppUser user = appUserRepository.findByUsername(username).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:/login?error";
        }

        session.setAttribute("loggedUser", user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}