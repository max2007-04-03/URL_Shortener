package ua.opnu.url_shortener.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByShortUrl(String shortUrl);
    List<ShortLink> findTop3ByUserIdOrderByVisitCountDesc(Long userId);

    @Query("SELECT COUNT(c) FROM LinkClick c WHERE c.link.user.id = :userId AND c.clickedAt >= :startOfDay")
    long countTodayClicksByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
}