package ua.opnu.url_shortener.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.opnu.url_shortener.auth.service.JwtService;
import ua.opnu.url_shortener.auth.entity.User;
import ua.opnu.url_shortener.auth.repository.UserRepository;
import ua.opnu.url_shortener.auth.config.JwtAuthenticationFilter; // Додано
import ua.opnu.url_shortener.auth.config.SecurityConfig;
import ua.opnu.url_shortener.link.entity.ShortLink;
import ua.opnu.url_shortener.link.service.ShortLinkService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortLinkService shortLinkService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getTodayStats_ShouldReturnCountAndMessage() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("max_polite");

        when(shortLinkService.getTodayClicksCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/v1/stats/today")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicksToday").value(5))
                .andExpect(jsonPath("$.message").value("Сьогодні ваші посилання використали 5 разів."));
    }

    @Test
    void getTopLinks_ShouldReturnListOfLinks() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);

        ShortLink link = new ShortLink();
        link.setShortUrl("abc1234");
        link.setVisitCount(100L);

        when(shortLinkService.getTopLinks(1L)).thenReturn(List.of(link));

        mockMvc.perform(get("/v1/stats/top")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl").value("abc1234"))
                .andExpect(jsonPath("$[0].visitCount").value(100));
    }

    @Test
    void getStats_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/v1/stats/today"))
                .andExpect(status().isForbidden());
    }
}