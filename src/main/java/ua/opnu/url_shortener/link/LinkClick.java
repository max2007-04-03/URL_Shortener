package ua.opnu.url_shortener.link;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "link_clicks")
@Getter
@Setter
public class LinkClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private ShortLink link;

    private LocalDateTime clickedAt;

    @Column(name = "ip_address")
    private String ipAddress;
}