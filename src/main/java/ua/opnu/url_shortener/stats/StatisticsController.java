package ua.opnu.url_shortener.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.opnu.url_shortener.auth.entity.User; // 1. Твій імпорт (перевір точний шлях до класу User)
import ua.opnu.url_shortener.link.entity.ShortLink;
import ua.opnu.url_shortener.link.service.ShortLinkService;

import java.util.List;

@RestController
@RequestMapping("/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Аналітика переходів за ТЗ")
public class StatisticsController {

    private final ShortLinkService shortLinkService;

    @GetMapping("/today")
    @Operation(summary = "Статистика за сьогодні",
            description = "Повертає сумарну кількість переходів по всіх посиланнях користувача за поточну добу")
    public ResponseEntity<TodayStatsResponse> getTodayStats(@AuthenticationPrincipal User currentUser) {
        long count = shortLinkService.getTodayClicksCount(currentUser.getId());

        String msg = String.format("Сьогодні ваші посилання використали %d разів.", count);
        return ResponseEntity.ok(new TodayStatsResponse(count, msg));
    }

    @GetMapping("/top")
    @Operation(summary = "Топ-3 найпопулярніших посилання",
            description = "Повертає три посилання користувача з найбільшою кількістю переходів")
    public ResponseEntity<List<ShortLink>> getTopLinks(@AuthenticationPrincipal User currentUser) {
        List<ShortLink> topLinks = shortLinkService.getTopLinks(currentUser.getId());
        return ResponseEntity.ok(topLinks);
    }
}