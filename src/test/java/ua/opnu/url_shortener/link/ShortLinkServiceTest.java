package ua.opnu.url_shortener.link;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ua.opnu.url_shortener.auth.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

    @Mock
    private ShortLinkRepository repository;

    @Mock
    private LinkClickRepository clickRepository;

    @InjectMocks
    private ShortLinkService shortLinkService;

    @Test
    void createShortLink_ShouldReturnCorrectLink() {
        String url = "https://google.com";
        User mockUser = new User();
        mockUser.setId(1L);

        when(repository.save(any(ShortLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortLink result = shortLinkService.createShortLink(url, mockUser);

        assertEquals(url, result.getOriginalUrl());
        assertEquals(mockUser, result.getUser());
        assertEquals(8, result.getShortUrl().length());
        assertNotNull(result.getExpiryDate());
        verify(repository, times(1)).save(any());
    }

    @Test
    void getOriginalAndIncrementCount_ShouldIncreaseCounter() {
        String shortUrl = "abc12345";
        String ip = "127.0.0.1";
        ShortLink link = new ShortLink();
        link.setShortUrl(shortUrl);
        link.setVisitCount(0L);
        link.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(link));
        when(repository.save(any(ShortLink.class))).thenReturn(link);
        when(clickRepository.findFirstByLinkAndIpAddressOrderByClickedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        ShortLink result = shortLinkService.getOriginalAndIncrementCount(shortUrl, ip);

        assertEquals(1L, result.getVisitCount());
        verify(repository).save(link);
        verify(clickRepository).save(any(LinkClick.class));
    }

    @Test
    void getTopLinks_ShouldReturnTopLinks() {
        Long userId = 1L;
        ShortLink link = new ShortLink();
        link.setVisitCount(100L);
        when(repository.findTop3ByUserIdOrderByVisitCountDesc(userId)).thenReturn(List.of(link));

        List<ShortLink> result = shortLinkService.getTopLinks(userId);

        assertFalse(result.isEmpty());
        assertEquals(100L, result.getFirst().getVisitCount());
        verify(repository).findTop3ByUserIdOrderByVisitCountDesc(userId);
    }

    @Test
    void getTodayClicksCount_ShouldReturnCount() {
        Long userId = 1L;
        when(repository.countTodayClicksByUser(eq(userId), any())).thenReturn(5L);

        long count = shortLinkService.getTodayClicksCount(userId);

        assertEquals(5L, count);
        verify(repository).countTodayClicksByUser(eq(userId), any());
    }

    @Test
    void deleteShortLink_ShouldDelete_WhenUserIsOwner() {
        String shortUrl = "abc12345";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        ShortLink link = new ShortLink();
        link.setUser(user);

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(link));

        shortLinkService.deleteShortLink(shortUrl, userId);

        verify(repository).delete(link);
    }

    @Test
    void deleteShortLink_ShouldThrowException_WhenUserIsNotOwner() {
        String shortUrl = "abc12345";
        User owner = new User(); owner.setId(1L);

        ShortLink link = new ShortLink();
        link.setUser(owner);

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(link));

        assertThrows(AccessDeniedException.class, () -> shortLinkService.deleteShortLink(shortUrl, 2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void getOriginalAndIncrementCount_ShouldThrowException_WhenLinkIsExpired() {
        String shortUrl = "expired1";
        ShortLink link = new ShortLink();
        link.setExpiryDate(LocalDateTime.now().minusDays(1)); // Термін вийшов

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(link));

        assertThrows(LinkExpiredException.class, () -> shortLinkService.getOriginalAndIncrementCount(shortUrl, "127.0.0.1"));
    }
}