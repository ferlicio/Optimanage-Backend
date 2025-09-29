package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceSyncResponse {

    private String marketplace;
    private LocalDateTime executedAt;
    private List<String> mensagens;
}
