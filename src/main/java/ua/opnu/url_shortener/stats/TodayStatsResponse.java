package ua.opnu.url_shortener.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodayStatsResponse {
    private long totalClicksToday;
    private String message;
}