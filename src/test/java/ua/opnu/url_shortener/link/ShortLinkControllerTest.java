package ua.opnu.url_shortener.link;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.opnu.url_shortener.auth.JwtAuthenticationFilter;
import ua.opnu.url_shortener.auth.JwtService;
import ua.opnu.url_shortener.auth.SecurityConfig;
import ua.opnu.url_shortener.auth.User;
import ua.opnu.url_shortener.auth.UserRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortLinkController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ShortLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortLinkService shortLinkService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void create_ShouldReturn201() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);

        ShortLink link = new ShortLink();
        link.setShortUrl("abc12345");
        link.setOriginalUrl("https://google.com");
        link.setExpiryDate(LocalDateTime.now());


        when(shortLinkService.createShortLink(anyString(), any())).thenReturn(link);

        mockMvc.perform(post("/v1/shorten")
                        .with(csrf())
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://google.com\"}"))
                .andExpect(status().isCreated());
    }
}