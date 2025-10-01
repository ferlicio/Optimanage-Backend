package com.AIT.Optimanage.Analytics.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformOrganizationsOverviewDTO {
    private List<TimeSeriesPoint> criadas;
    private List<TimeSeriesPoint> assinadas;
    private long totalAtivas;
    private long totalInativas;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSeriesPoint {
        private LocalDate data;
        private long quantidade;
    }
}
