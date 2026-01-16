package com.example.demo.model;

import com.example.demo.enums.AlertAction;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_data_id")
    private SensorData sensorData;

    @Enumerated(EnumType.STRING)
    private AlertAction action;

    @Column(name = "threshold")
    private Double threshold;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved")
    private boolean resolved = false;

    // Constructors
    public Alert() {}

    public Alert(SensorData sensorData, AlertAction action, Double threshold, String message) {
        this.sensorData = sensorData;
        this.action = action;
        this.threshold = threshold;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SensorData getSensorData() {
        return sensorData;
    }

    public void setSensorData(SensorData sensorData) {
        this.sensorData = sensorData;
    }

    public AlertAction getAction() {
        return action;
    }

    public void setAction(AlertAction action) {
        this.action = action;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
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
