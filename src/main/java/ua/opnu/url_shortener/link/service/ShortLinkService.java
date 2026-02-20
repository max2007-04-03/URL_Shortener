package ua.opnu.url_shortener.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.opnu.url_shortener.auth.entity.User;
import ua.opnu.url_shortener.link.dto.ShortLinkUpdateRequest;
import ua.opnu.url_shortener.link.entity.LinkClick;
import ua.opnu.url_shortener.link.entity.ShortLink;
import ua.opnu.url_shortener.link.exception.LinkExpiredException;
import ua.opnu.url_shortener.link.exception.LinkNotFoundException;
import ua.opnu.url_shortener.link.repository.LinkClickRepository;
import ua.opnu.url_shortener.link.repository.ShortLinkRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

    // КОНСТАНТА для усунення дублювання
    private static final String LINK_NOT_FOUND_MSG = "Посилання не знайдено";

    private final ShortLinkRepository repository;
    private final LinkClickRepository clickRepository;

    @Transactional
    public ShortLink createShortLink(String originalUrl, User user) {
        ShortLink link = new ShortLink();
        link.setOriginalUrl(originalUrl);
        link.setShortUrl(UUID.randomUUID().toString().substring(0, 8));
        link.setCreatedAt(LocalDateTime.now());
        link.setExpiryDate(LocalDateTime.now().plusDays(30));
        link.setVisitCount(0L);
        link.setUser(user);
        return repository.save(link);
    }

    @Transactional
    public ShortLink updateShortLink(String shortUrl, ShortLinkUpdateRequest request, Long userId) {
        ShortLink link = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new LinkNotFoundException(LINK_NOT_FOUND_MSG));

        if (link.getUser() == null || !link.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас немає прав на редагування цього посилання");
        }

        if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            link.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getExpiryDate() != null) {
            link.setExpiryDate(request.getExpiryDate());
        }

        return repository.save(link);
    }

    public List<ShortLink> getUserLinks(Long userId, boolean activeOnly) {
        List<ShortLink> links = repository.findAllByUserId(userId);

        if (activeOnly) {
            LocalDateTime now = LocalDateTime.now();
            return links.stream()
                    .filter(link -> link.getExpiryDate() == null || link.getExpiryDate().isAfter(now))
                    .toList();
        }
        return links;
    }

    @Transactional
    public void deleteShortLink(String shortUrl, Long userId) {
        ShortLink link = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new LinkNotFoundException(LINK_NOT_FOUND_MSG));

        if (link.getUser() == null || !link.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас немає прав на видалення цього посилання");
        }
        repository.delete(link);
    }

    @Transactional
    public ShortLink getOriginalAndIncrementCount(String shortUrl, String ip) {
        ShortLink link = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new LinkNotFoundException(LINK_NOT_FOUND_MSG));

        if (link.getExpiryDate() != null && link.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new LinkExpiredException("Термін дії посилання вичерпано!");
        }

        boolean shouldIncrement = clickRepository.findFirstByLinkAndIpAddressOrderByClickedAtDesc(link, ip)
                .map(lastClick -> lastClick.getClickedAt().isBefore(LocalDateTime.now().minusMinutes(1)))
                .orElse(true);

        if (shouldIncrement) {
            link.setVisitCount(link.getVisitCount() + 1);
            repository.save(link);
        }

        LinkClick click = new LinkClick();
        click.setLink(link);
        click.setIpAddress(ip);
        click.setClickedAt(LocalDateTime.now());
        clickRepository.save(click);

        return link;
    }

    public long getTodayClicksCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return repository.countTodayClicksByUser(userId, startOfDay);
    }

    public List<ShortLink> getTopLinks(Long userId) {
        return repository.findTop3ByUserIdOrderByVisitCountDesc(userId);
    }
}