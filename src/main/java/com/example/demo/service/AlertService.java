package com.example.demo.service;

import com.example.demo.dto.AlertDTO;
import com.example.demo.enums.AlertAction;
import com.example.demo.model.Alert;
import com.example.demo.model.SensorData;
import com.example.demo.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;

    /**
     * Получить все тревоги
     */
    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Получить все тревоги для конкретного автобуса
     */
    public List<AlertDTO> getAlertsByBusId(Long busId) {
        return alertRepository.findAll().stream()
            .filter(a -> a.getSensorData().getBus() != null && 
                         a.getSensorData().getBus().getId().equals(busId))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Получить тревоги за определённый период времени
     */
    public List<AlertDTO> getAlertsByDateRange(LocalDateTime from, LocalDateTime to) {
        return alertRepository.findAll().stream()
            .filter(a -> a.getCreatedAt() != null && 
                         !a.getCreatedAt().isBefore(from) && 
                         !a.getCreatedAt().isAfter(to))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Получить тревоги для автобуса за период времени
     */
    public List<AlertDTO> getAlertsByBusAndDateRange(Long busId, LocalDateTime from, LocalDateTime to) {
        return alertRepository.findAll().stream()
            .filter(a -> a.getSensorData().getBus() != null && 
                         a.getSensorData().getBus().getId().equals(busId) &&
                         a.getCreatedAt() != null && 
                         !a.getCreatedAt().isBefore(from) && 
                         !a.getCreatedAt().isAfter(to))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Создать новую тревогу
     */
    public Alert createAlert(SensorData sensorData, AlertAction action, String message) {
        Alert alert = new Alert();
        alert.setSensorData(sensorData);
        alert.setAction(action);
        alert.setMessage(message);
        alert.setThreshold(sensorData.getValue());
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    /**
     * Получить тревогу по ID
     */
    public AlertDTO getAlertById(Long id) {
        return alertRepository.findById(id)
            .map(this::convertToDTO)
            .orElse(null);
    }

    /**
     * Удалить тревогу
     */
    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    /**
     * Преобразовать Alert в AlertDTO
     */
    private AlertDTO convertToDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setSensorDataId(alert.getSensorData().getId());
        dto.setSensorType(alert.getSensorData().getSensorType());
        dto.setValue(alert.getSensorData().getValue());
        dto.setThreshold(alert.getThreshold());
        dto.setAction(alert.getAction());
        dto.setMessage(alert.getMessage());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }
}
