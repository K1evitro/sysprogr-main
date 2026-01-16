package com.example.demo.dto;

import com.example.demo.enums.AlertAction;
import com.example.demo.model.SensorType;
import java.time.LocalDateTime;

public class AlertDTO {
    private Long id;
    private Long sensorDataId;
    private SensorType sensorType;  // Измени на SensorType enum
    private Double value;
    private Double threshold;
    private AlertAction action;
    private String message;
    private LocalDateTime createdAt;
    private boolean resolved;

    // Constructors
    public AlertDTO() {}

    public AlertDTO(Long id, Long sensorDataId, SensorType sensorType, Double value, 
                    Double threshold, AlertAction action, String message, 
                    LocalDateTime createdAt, boolean resolved) {
        this.id = id;
        this.sensorDataId = sensorDataId;
        this.sensorType = sensorType;
        this.value = value;
        this.threshold = threshold;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
        this.resolved = resolved;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSensorDataId() {
        return sensorDataId;
    }

    public void setSensorDataId(Long sensorDataId) {
        this.sensorDataId = sensorDataId;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public AlertAction getAction() {
        return action;
    }

    public void setAction(AlertAction action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
