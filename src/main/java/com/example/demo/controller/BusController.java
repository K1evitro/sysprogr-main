package com.example.demo.controller;

import com.example.demo.model.Bus;
import com.example.demo.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@Tag(name = "bus-controller", description = "API для управления автобусами в системе мониторинга")
public class BusController {

    private static final Logger log = LoggerFactory.getLogger(BusController.class);

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PreAuthorize("hasAuthority('READ_BUS')")
    @GetMapping
    @Operation(
        summary = "Получить список всех автобусов",
        description = "Возвращает полный список всех зарегистрированных автобусов в системе мониторинга"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список автобусов успешно получен",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Bus.class))),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён - недостаточно прав", content = @Content)
    })
    public ResponseEntity<List<Bus>> getAllBuses() {
        log.info("Запрос на получение списка всех автобусов");
        try {
            List<Bus> buses = busService.getAllBuses();
            log.info("Найдено автобусов: {}", buses.size());
            return ResponseEntity.ok(buses);
        } catch (Exception e) {
            log.error("Ошибка при получении списка автобусов: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('WRITE_BUS')")
    @PostMapping
    @Operation(
        summary = "Зарегистрировать новый автобус",
        description = "Создаёт новую запись автобуса в системе. Требуется указать модель автобуса."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Автобус успешно зарегистрирован",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Bus.class))),
        @ApiResponse(responseCode = "400", description = "Некорректные данные автобуса", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён - недостаточно прав", content = @Content)
    })
    public ResponseEntity<Bus> createBus(
        @Parameter(description = "Данные нового автобуса (модель обязательна)", required = true,
            schema = @Schema(example = "{\"model\": \"KAMAZ\"}"))
        @RequestBody Bus bus
    ) {
        log.info("Создание нового автобуса: модель={}", bus.getModel());
        try {
            Bus createdBus = busService.createBus(bus);
            log.info("Автобус успешно создан с ID: {}", createdBus.getId());
            return ResponseEntity.ok(createdBus);
        } catch (Exception e) {
            log.error("Ошибка при создании автобуса: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('WRITE_BUS')")
    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить данные автобуса",
        description = "Обновляет информацию о существующем автобусе по его идентификатору"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные автобуса успешно обновлены",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Bus.class))),
        @ApiResponse(responseCode = "404", description = "Автобус с указанным ID не найден", content = @Content),
        @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён - недостаточно прав", content = @Content)
    })
    public ResponseEntity<Bus> updateBus(
        @Parameter(description = "ID автобуса для обновления", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Обновлённые данные автобуса", required = true,
            schema = @Schema(example = "{\"model\": \"PAZ\"}"))
        @RequestBody Bus bus
    ) {
        log.info("Обновление автобуса с ID: {}", id);
        try {
            Bus updatedBus = busService.updateBus(id, bus);
            if (updatedBus != null) {
                log.info("Автобус ID={} успешно обновлён", id);
                return ResponseEntity.ok(updatedBus);
            } else {
                log.warn("Автобус с ID={} не найден", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при обновлении автобуса ID={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('DELETE_BUS')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить автобус из системы",
        description = "Удаляет автобус из системы мониторинга по указанному идентификатору"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Автобус успешно удалён", content = @Content),
        @ApiResponse(responseCode = "404", description = "Автобус с указанным ID не найден", content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён - недостаточно прав", content = @Content)
    })
    public ResponseEntity<Void> deleteBus(
        @Parameter(description = "ID автобуса для удаления", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.warn("Запрос на удаление автобуса с ID: {}", id);
        try {
            boolean deleted = busService.deleteBus(id);
            if (deleted) {
                log.info("Автобус ID={} успешно удалён", id);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Автобус с ID={} не найден для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении автобуса ID={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
