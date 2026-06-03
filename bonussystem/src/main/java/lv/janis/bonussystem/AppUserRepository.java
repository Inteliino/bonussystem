package lv.janis.bonussystem;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.janis.bonussystem.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}