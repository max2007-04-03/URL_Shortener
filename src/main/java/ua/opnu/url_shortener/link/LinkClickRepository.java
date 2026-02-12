package ua.opnu.url_shortener.link;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LinkClickRepository extends JpaRepository<LinkClick, Long> {
    Optional<LinkClick> findFirstByLinkAndIpAddressOrderByClickedAtDesc(ShortLink link, String ipAddress);
}