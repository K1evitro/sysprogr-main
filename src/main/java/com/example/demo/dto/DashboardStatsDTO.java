package com.example.demo.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
   
    private int totalBuses;
    private int totalSensors;
    private int totalAnomalies;
    
    
    private int anomaliesLast24h;
    private double averageSensorValue;
    private double maxSensorValue;
    private double minSensorValue;
    
    
    private Map<String, Integer> anomaliesBySeverity; 
    
    private Map<String, Double> averageValueBySensorType;
    
    private int activeBuses;

    private List<AlertSummaryDTO> recentAlerts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertSummaryDTO {
        private Long id;
        private String sensorType;
        private String action;
        private Long busId;
        private Double value;
        private String timestamp;
    }
}
