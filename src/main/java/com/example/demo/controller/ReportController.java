package com.example.demo.controller;

import com.example.demo.dto.AnomalyReportDTO;
import com.example.demo.repository.SensorDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "report-controller", description = "Отчёты по работе системы мониторинга")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final SensorDataRepository sensorDataRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public ReportController(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @PreAuthorize("hasAuthority('READ_REPORT')")
    @GetMapping("/anomalies")
    @Operation(
        summary = "Отчёт по аномалиям датчиков",
        description = "Возвращает количество аномалий по каждому автобусу за указанный период времени"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Отчёт успешно сформирован",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AnomalyReportDTO.class))),
        @ApiResponse(responseCode = "400", description = "Некорректный формат даты", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public List<AnomalyReportDTO> getAnomaliesReport(
            @Parameter(description = "Дата начала периода в формате ISO (yyyy-MM-dd'T'HH:mm:ss)",
                       required = true, example = "2026-01-01T00:00:00")
            @RequestParam String from,
            @Parameter(description = "Дата окончания периода в формате ISO (yyyy-MM-dd'T'HH:mm:ss)",
                       required = true, example = "2026-01-31T23:59:59")
            @RequestParam String to
    ) {
        log.info("Формирование отчёта по аномалиям: from={}, to={}", from, to);
        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, formatter);
            LocalDateTime toDate = LocalDateTime.parse(to, formatter);

            List<AnomalyReportDTO> report =
                    sensorDataRepository.getAnomalyReportByBus(fromDate, toDate);

            log.info("Отчёт по аномалиям сформирован, записей: {}", report.size());
            return report;
        } catch (Exception e) {
            log.error("Ошибка при формировании отчёта по аномалиям: {}, {}", e.getMessage(), e);
            throw new RuntimeException("Invalid date format. Use ISO_DATE_TIME format", e);
        }
    }
}
