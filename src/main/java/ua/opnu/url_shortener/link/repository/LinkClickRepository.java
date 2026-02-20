package ua.opnu.url_shortener.link.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.opnu.url_shortener.link.entity.LinkClick;
import ua.opnu.url_shortener.link.entity.ShortLink;

import java.util.Optional;

public interface LinkClickRepository extends JpaRepository<LinkClick, Long> {
    Optional<LinkClick> findFirstByLinkAndIpAddressOrderByClickedAtDesc(ShortLink link, String ipAddress);
}