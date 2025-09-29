package com.AIT.Optimanage.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.recommendation")
public class RecommendationProperties {

    private int historyWindowDays = 180;
    private double churnWeight = 0.5;
    private double rotatividadeWeight = 0.25;
    private double produtoMargemWeight = 1.0;
    private double servicoMargemWeight = 1.0;
    private double bundleWeight = 1.2;
}
