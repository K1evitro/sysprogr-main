package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.SensorData;
import com.example.demo.service.SensorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/upload")
@Tag(name = "file-controller", description = "API для загрузки файлов и привязки их к данным датчиков")
public class FileController {

    private final SensorService sensorService;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public FileController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PreAuthorize("hasAuthority('SENSOR_WRITE')")
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Загрузить файл к записи датчика",
        description = "Загружает файл (например, фото, документ или отчёт) и привязывает его к конкретной записи датчика. Файл сохраняется на сервере, а путь к нему записывается в базу данных."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Файл успешно загружен и привязан",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Ошибка при загрузке файла (некорректный формат или повреждённый файл)", 
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content),
        @ApiResponse(responseCode = "404", description = "Запись датчика не найдена", content = @Content)
    })
    public ResponseEntity<String> uploadFile(
            @Parameter(description = "ID записи датчика, к которой привязывается файл", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(
                description = "Загружаемый файл (multipart/form-data)", 
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файл не может быть пустым");
            }

            // Находим запись датчика - используем существующий метод
            SensorData sensorData = sensorService.getAll().stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Sensor data not found with id: " + id));

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = uploadDir + "/" + uniqueFilename;
            sensorData.setFilePath(relativePath);
            sensorService.updateSensorData(id, sensorData);

            return ResponseEntity.ok(relativePath);

        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("Ошибка при сохранении файла: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
