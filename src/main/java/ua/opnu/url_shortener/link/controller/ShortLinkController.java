package ua.opnu.url_shortener.link.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.opnu.url_shortener.auth.entity.User;
import ua.opnu.url_shortener.link.dto.ShortLinkRequest;
import ua.opnu.url_shortener.link.dto.ShortLinkResponse;
import ua.opnu.url_shortener.link.dto.ShortLinkUpdateRequest;
import ua.opnu.url_shortener.link.entity.ShortLink;
import ua.opnu.url_shortener.link.service.ShortLinkService;

import java.net.URI;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ShortLink>> getUserLinks(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal User currentUser
    ) {
        List<ShortLink> links = shortLinkService.getUserLinks(currentUser.getId(), activeOnly);
        return ResponseEntity.ok(links);
    }

    @PatchMapping("/{shortUrl}")
    public ResponseEntity<ShortLinkResponse> updateLink(
            @PathVariable String shortUrl,
            @Valid @RequestBody ShortLinkUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ShortLink link = shortLinkService.updateShortLink(shortUrl, request, currentUser.getId());
        return ResponseEntity.ok(new ShortLinkResponse(link.getShortUrl(), link.getOriginalUrl(), link.getExpiryDate()));
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