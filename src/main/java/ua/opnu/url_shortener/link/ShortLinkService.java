package ua.opnu.url_shortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.opnu.url_shortener.auth.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private final ShortLinkRepository repository;
    private final LinkClickRepository clickRepository;

    @Transactional
    public ShortLink createShortLink(String originalUrl, User user) {
        ShortLink link = new ShortLink();
        link.setOriginalUrl(originalUrl);
        // Генеруємо унікальний код довжиною 8 символів
        link.setShortUrl(UUID.randomUUID().toString().substring(0, 8));
        link.setExpiryDate(LocalDateTime.now().plusDays(30));
        link.setVisitCount(0L);
        link.setUser(user);
        return repository.save(link);
    }

    @Transactional
    public void deleteShortLink(String shortUrl, Long userId) {
        ShortLink link = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new LinkNotFoundException("Посилання не знайдено"));

        // Перевірка власника для захисту від видалення чужих даних
        if (link.getUser() == null || !link.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас немає прав на видалення цього посилання");
        }
        repository.delete(link);
    }

    @Transactional
    public ShortLink getOriginalAndIncrementCount(String shortUrl, String ip) {
        ShortLink link = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new LinkNotFoundException("Посилання не знайдено"));

        // 👇 ПЕРЕВІРКА ТЕРМІНУ ДІЇ (Виправляє попередження 'LinkExpiredException' is never used)
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