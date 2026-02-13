package org.tus.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tus.payment.analytics.BusinessMetrics;
import org.tus.payment.service.BusinessAnalyticsService;

import java.util.Date;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {
    private final BusinessAnalyticsService businessAnalyticsService;

    /**
     * Cross-shard: get overall business metrics for a date range.
     * Queries all shards and merges results.
     *
     * @param startDate optional, ISO date (yyyy-MM-dd) or empty for no lower bound
     * @param endDate   optional, ISO date (yyyy-MM-dd) or empty for no upper bound
     */
    @GetMapping("/analytics/overall")
    public ResponseEntity<BusinessMetrics> getOverallMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        BusinessMetrics metrics = businessAnalyticsService.getOverallMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
}
