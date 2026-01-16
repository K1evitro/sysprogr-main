package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.SensorData;
import com.example.demo.service.SensorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/export")
@Tag(name = "export-controller", description = "API для экспорта данных")
public class ExportController {
    
    private final SensorService sensorService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExportController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PreAuthorize("hasAuthority('SENSOR_READ')")
    @GetMapping("/sensors/excel")
    @Operation(
        summary = "Экспорт данных датчиков в Excel",
        description = "Выгружает все данные датчиков в файл Excel (.xlsx)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Файл успешно сгенерирован"),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
        @ApiResponse(responseCode = "500", description = "Ошибка генерации файла")
    })
    public ResponseEntity<byte[]> exportSensorsToExcel(
            @Parameter(description = "Фильтр по ID автобуса (опционально)")
            @RequestParam(required = false) Long busId
    ) throws IOException {
        
        List<SensorData> sensorDataList;
        if (busId != null) {
            sensorDataList = sensorService.getSensorDataByBusId(busId);
        } else {
            sensorDataList = sensorService.getAll();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sensor Data");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Тип датчика", "Значение", "Время записи", "Аномалия", "ID автобуса", "Путь к файлу"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (SensorData data : sensorDataList) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(data.getId());
                row.createCell(1).setCellValue(String.valueOf(data.getSensorType())); // FIX: преобразуем в String
                row.createCell(2).setCellValue(data.getValue());
                row.createCell(3).setCellValue(data.getTimestamp().format(formatter));
                row.createCell(4).setCellValue(data.isAnomaly() ? "Да" : "Нет");
                row.createCell(5).setCellValue(data.getBus() != null ? data.getBus().getId() : 0);
                row.createCell(6).setCellValue(data.getFilePath() != null ? data.getFilePath() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "sensor_data_export.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        }
    }

    @PreAuthorize("hasAuthority('SENSOR_READ')")
    @GetMapping("/sensors/csv")
    @Operation(
        summary = "Экспорт данных датчиков в CSV",
        description = "Выгружает все данные датчиков в файл CSV"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Файл успешно сгенерирован"),
        @ApiResponse(responseCode = "401", description = "Неавторизованный доступ"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    public ResponseEntity<byte[]> exportSensorsToCsv(
            @Parameter(description = "Фильтр по ID автобуса (опционально)")
            @RequestParam(required = false) Long busId
    ) {
        
        List<SensorData> sensorDataList;
        if (busId != null) {
            sensorDataList = sensorService.getSensorDataByBusId(busId);
        } else {
            sensorDataList = sensorService.getAll();
        }

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Тип датчика,Значение,Время записи,Аномалия,ID автобуса,Путь к файлу\n");
        
        for (SensorData data : sensorDataList) {
            csv.append(data.getId()).append(",")
               .append(String.valueOf(data.getSensorType())).append(",") // FIX: преобразуем в String
               .append(data.getValue()).append(",")
               .append(data.getTimestamp().format(formatter)).append(",")
               .append(data.isAnomaly() ? "Да" : "Нет").append(",")
               .append(data.getBus() != null ? data.getBus().getId() : "").append(",")
               .append(data.getFilePath() != null ? data.getFilePath() : "").append("\n");
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "sensor_data_export.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
