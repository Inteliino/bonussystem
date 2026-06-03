package lv.janis.bonussystem.controller;

import lv.janis.bonussystem.AppUserRepository;
import lv.janis.bonussystem.model.AppUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", appUserRepository.findAll());
        return "users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute AppUser user) {
        appUserRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String role) {

        AppUser user = appUserRepository.findById(id).orElseThrow();

        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        appUserRepository.save(user);

        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        appUserRepository.deleteById(id);
        return "redirect:/users";
    }
}