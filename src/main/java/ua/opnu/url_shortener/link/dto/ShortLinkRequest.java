package ua.opnu.url_shortener.link.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.Data;

@Data
public class ShortLinkRequest {
    @NotBlank(message = "Original URL is required")
    @URL(message = "Invalid URL format")
    private String originalUrl;
}