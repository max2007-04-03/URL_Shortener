package ua.opnu.url_shortener.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.opnu.url_shortener.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
      Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}