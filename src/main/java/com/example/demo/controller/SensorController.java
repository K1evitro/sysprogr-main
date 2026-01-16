package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SensorDataCreateDTO;
import com.example.demo.model.Bus;
import com.example.demo.model.SensorData;
import com.example.demo.service.SensorService;
import com.example.demo.service.BusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "sensor-controller", description = "API для управления данными датчиков автобусов")
public class SensorController {

    private static final Logger log = LoggerFactory.getLogger(SensorController.class);

    private final SensorService sensorService;
    private final BusService busService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public SensorController(SensorService sensorService, BusService busService) {
        this.sensorService = sensorService;
        this.busService = busService;
    }

    // === CREATE ===
    @PreAuthorize("hasAuthority('WRITE_SENSOR')")
    @PostMapping
    @Operation(
        summary = "Создать новую запись датчика",
        description = "Создаёт новую запись с данными от датчика автобуса. Требуется наличие автобуса с указанным ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Запись успешно создана",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content),
        @ApiResponse(responseCode = "404", description = "Автобус не найден", content = @Content)
    })
    public ResponseEntity<SensorData> createSensorData(
        @Parameter(description = "Данные для создания записи датчика", required = true)
        @RequestBody SensorDataCreateDTO dto
    ) {
        log.info("Создание записи датчика для автобуса busId={}, type={}, value={}, anomaly={}",
                dto.getBusId(), dto.getSensorType(), dto.getValue(), dto.isAnomaly());
        try {
            Bus bus = busService.getBusById(dto.getBusId())
                    .orElseThrow(() -> new RuntimeException("Bus not found"));

            SensorData sensorData = new SensorData();
            sensorData.setBusId(bus.getId());
            sensorData.setSensorType(dto.getSensorType());
            sensorData.setValue(dto.getValue());
            sensorData.setTimestamp(dto.getTimestamp());
            sensorData.setAnomaly(dto.isAnomaly());
            sensorData.setBus(bus);

            SensorData saved = sensorService.createSensorData(sensorData);
            log.info("Запись датчика успешно создана с id={} для автобуса busId={}", saved.getId(), dto.getBusId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Ошибка при создании записи датчика для автобуса busId={}: {}", dto.getBusId(), e.getMessage(), e);
            throw e;
        }
    }

    // === READ ALL ===
    @PreAuthorize("hasAuthority('READ_SENSOR')")
    @GetMapping
    @Operation(
        summary = "Получить все данные датчиков",
        description = "Возвращает список всех данных от датчиков с возможностью пагинации и фильтрации по типу датчика"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список успешно получен",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public List<SensorData> getAllSensorData(
            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Фильтр по типу датчика (ENGINE_TEMP, TIRE_PRESSURE, FUEL_LEVEL)")
            @RequestParam(required = false) String type
    ) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Запрос всех данных датчиков: page={}, size={}, type={}", page, size, type);
        try {
            List<SensorData> result = sensorService.getAll();
            log.info("Получено записей датчиков: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Ошибка при получении списка данных датчиков: {}", e.getMessage(), e);
            throw e;
        }
    }

    // === READ BY BUS ===
    @PreAuthorize("hasAuthority('READ_SENSOR')")
    @GetMapping("/{busId}")
    @Operation(
        summary = "Получить данные датчиков по ID автобуса",
        description = "Возвращает все записи датчиков для конкретного автобуса"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные успешно получены",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public List<SensorData> getSensorDataByBusId(
        @Parameter(description = "ID автобуса", required = true, example = "1")
        @PathVariable Long busId
    ) {
        log.info("Запрос данных датчиков по busId={}", busId);
        try {
            List<SensorData> list = sensorService.getSensorDataByBusId(busId);
            log.info("Для автобуса busId={} найдено записей датчиков: {}", busId, list.size());
            return list;
        } catch (Exception e) {
            log.error("Ошибка при получении данных датчиков по busId={}: {}", busId, e.getMessage(), e);
            throw e;
        }
    }

    // === UPDATE ===
    @PreAuthorize("hasAuthority('WRITE_SENSOR')")
    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить данные датчика",
        description = "Обновляет существующую запись датчика по её идентификатору"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Запись успешно обновлена",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "404", description = "Запись не найдена", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public ResponseEntity<SensorData> updateSensorData(
        @Parameter(description = "ID записи для обновления", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Обновлённые данные датчика", required = true)
        @RequestBody @Valid SensorData updatedSensorData
    ) {
        log.info("Обновление данных датчика id={}", id);
        try {
            SensorData sensorData = sensorService.updateSensorData(id, updatedSensorData);
            if (sensorData != null) {
                log.info("Запись датчика id={} успешно обновлена", id);
                return ResponseEntity.ok(sensorData);
            } else {
                log.warn("Запись датчика id={} не найдена для обновления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при обновлении данных датчика id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // === DELETE ===
    @PreAuthorize("hasAuthority('DELETE_SENSOR')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить запись датчика",
        description = "Удаляет запись датчика по указанному идентификатору"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Запись успешно удалена", content = @Content),
        @ApiResponse(responseCode = "404", description = "Запись не найдена", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public ResponseEntity<Void> deleteSensorData(
        @Parameter(description = "ID записи для удаления", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("Удаление записи датчика id={}", id);
        try {
            boolean deleted = sensorService.deleteSensorData(id);
            if (deleted) {
                log.info("Запись датчика id={} успешно удалена", id);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Запись датчика id={} не найдена для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении записи датчика id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // === LATEST ===
    @PreAuthorize("hasAuthority('READ_SENSOR')")
    @GetMapping("/latest")
    @Operation(
        summary = "Получить последние данные датчиков",
        description = "Возвращает самые свежие показания датчиков для указанного автобуса"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные успешно получены",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "404", description = "Данные не найдены", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public ResponseEntity<SensorData> getLatestSensorData(
        @Parameter(description = "ID автобуса", required = true, example = "1")
        @RequestParam Long busId
    ) {
        log.info("Запрос последних данных датчиков для автобуса busId={}", busId);
        try {
            return sensorService.getLatestSensorDataByBusId(busId)
                    .map(data -> {
                        log.info("Найдены последние данные датчиков для автобуса busId={} с id={}", busId, data.getId());
                        return ResponseEntity.ok(data);
                    })
                    .orElseGet(() -> {
                        log.warn("Последние данные датчиков для автобуса busId={} не найдены", busId);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Ошибка при получении последних данных датчиков для автобуса busId={}: {}", busId, e.getMessage(), e);
            throw e;
        }
    }

    // === HISTORY ===
    @PreAuthorize("hasAuthority('READ_SENSOR')")
    @GetMapping("/history")
    @Operation(
        summary = "Получить историю данных датчиков",
        description = "Возвращает исторические данные датчиков за указанный период времени. Можно фильтровать по конкретному автобусу."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "История успешно получена",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SensorData.class))),
        @ApiResponse(responseCode = "400", description = "Некорректный формат даты", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content)
    })
    public List<SensorData> getSensorHistory(
            @Parameter(description = "Дата начала периода в формате ISO (yyyy-MM-dd'T'HH:mm:ss)",
                       required = true, example = "2025-12-01T00:00:00")
            @RequestParam String from,
            @Parameter(description = "Дата окончания периода в формате ISO (yyyy-MM-dd'T'HH:mm:ss)",
                       required = true, example = "2025-12-31T23:59:59")
            @RequestParam String to,
            @Parameter(description = "ID автобуса для фильтрации (опционально)", example = "1")
            @RequestParam(required = false) Long busId
    ) {
        log.info("Запрос истории данных датчиков: from={}, to={}, busId={}", from, to, busId);
        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, formatter);
            LocalDateTime toDate = LocalDateTime.parse(to, formatter);

            List<SensorData> result;
            if (busId != null) {
                result = sensorService.getSensorHistoryByBusAndPeriod(busId, fromDate, toDate);
                log.info("История датчиков для автобуса busId={} за период [{} - {}], записей: {}",
                        busId, fromDate, toDate, result.size());
            } else {
                result = sensorService.getSensorHistoryByPeriod(fromDate, toDate);
                log.info("История датчиков за период [{} - {}], записей: {}", fromDate, toDate, result.size());
            }
            return result;
        } catch (Exception e) {
            log.error("Ошибка при получении истории датчиков: from={}, to={}, busId={}, ошибка={}",
                    from, to, busId, e.getMessage(), e);
            throw new RuntimeException(
                    "Invalid date format. Use ISO_DATE_TIME format (e.g., 2026-01-16T12:00:00)", e);
        }
    }
}
