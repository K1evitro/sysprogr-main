package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "dashboard-controller", description = "API для дашборда с статистикой")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/stats")
    @Operation(summary = "Получить статистику для дашборда")
    public ResponseEntity<?> getDashboardStats() {
        log.info("Запрос статистики для дашборда");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBuses", 5);
            stats.put("totalSensors", 25);
            stats.put("totalAnomalies", 42);
            stats.put("anomaliesLast24h", 8);
            stats.put("averageSensorValue", 65.5);
            stats.put("maxSensorValue", 98.2);
            stats.put("minSensorValue", 12.5);

            Map<String, Integer> anomaliesBySeverity = new HashMap<>();
            anomaliesBySeverity.put("error", 2);
            anomaliesBySeverity.put("warning", 4);
            anomaliesBySeverity.put("ok", 2);
            stats.put("anomaliesBySeverity", anomaliesBySeverity);

            stats.put("activeBuses", 5);
            stats.put("recentAlerts", new ArrayList<>());

            log.info("Статистика для дашборда сформирована: totalBuses={}, totalSensors={}, totalAnomalies={}, anomaliesLast24h={}",
                    stats.get("totalBuses"), stats.get("totalSensors"), stats.get("totalAnomalies"), stats.get("anomaliesLast24h"));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Ошибка при формировании статистики для дашборда: {}", e.getMessage(), e);
            throw e;
        }
    }
}
