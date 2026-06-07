package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.AppUserRepository;
import lv.janis.bonussystem.model.AppUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(AppUserRepository appUserRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (user == null) {
            return "redirect:/login?error=true";
        }

        String dbPassword = user.getPassword();

        boolean passwordOk;

        if (dbPassword != null && dbPassword.startsWith("$2")) {
            passwordOk = passwordEncoder.matches(password, dbPassword);
        } else {
            passwordOk = password.equals(dbPassword);

            if (passwordOk) {
                user.setPassword(passwordEncoder.encode(password));
                appUserRepository.save(user);
            }
        }

        if (!passwordOk) {
            return "redirect:/login?error=true";
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