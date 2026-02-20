package ua.opnu.url_shortener.link.dto;

import org.hibernate.validator.constraints.URL;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShortLinkUpdateRequest {
    @URL(message = "Invalid URL format")
    private String originalUrl;

    private LocalDateTime expiryDate;
}