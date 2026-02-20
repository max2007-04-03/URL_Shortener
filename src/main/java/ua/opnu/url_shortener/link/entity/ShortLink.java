package ua.opnu.url_shortener.link.entity;

import jakarta.persistence.*;
import lombok.Data;
import ua.opnu.url_shortener.auth.entity.User;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "short_links")
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String shortUrl;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    private LocalDateTime createdAt;

    private LocalDateTime expiryDate;

    private Long visitCount = 0L;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}