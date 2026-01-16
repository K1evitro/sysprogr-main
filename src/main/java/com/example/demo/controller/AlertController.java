package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.SensorData;
import com.example.demo.service.SensorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "alert-controller", description = "API для получения уведомлений об аномалиях датчиков")
public class AlertController {

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);

    private final SensorService sensorService;

    public AlertController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    @Operation(
        summary = "Получить все аномалии",
        description = "Возвращает список всех записей датчиков с обнаруженными аномалиями (anomaly = true). " +
                      "Используется для мониторинга критических состояний автобусов."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список аномалий успешно получен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён - недостаточно прав", content = @Content)
    })
    public List<SensorData> getAllAlerts() {
        log.info("Запрос списка аномалий датчиков");
        try {
            List<SensorData> anomalies = sensorService.getAllAnomalies();
            log.info("Получено аномалий датчиков: {}", anomalies.size());
            return anomalies;
        } catch (Exception e) {
            log.error("Ошибка при получении списка аномалий датчиков: {}", e.getMessage(), e);
            throw e;
        }
    }
}
