package ua.opnu.url_shortener.link;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.opnu.url_shortener.auth.User;

import java.net.URI;

@RestController
@RequestMapping("/v1/shorten")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping
    public ResponseEntity<ShortLinkResponse> create(
            @Valid @RequestBody ShortLinkRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ShortLink link = shortLinkService.createShortLink(request.getOriginalUrl(), currentUser);
        return new ResponseEntity<>(new ShortLinkResponse(link.getShortUrl(), link.getOriginalUrl(), link.getExpiryDate()), HttpStatus.CREATED);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortUrl, HttpServletRequest request) {
        ShortLink link = shortLinkService.getOriginalAndIncrementCount(shortUrl, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getOriginalUrl())).build();
    }

    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortUrl, @AuthenticationPrincipal User currentUser) {
        shortLinkService.deleteShortLink(shortUrl, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}