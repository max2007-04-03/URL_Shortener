package ua.opnu.url_shortener.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShortLinkResponse {

    private String shortUrl;

    private String originalUrl;

    private LocalDateTime expiryDate;

}