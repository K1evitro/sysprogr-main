package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "anomaly", nullable = false)
    private boolean anomaly = false;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", insertable = false, updatable = false)
    @JsonIgnore
    private Bus bus;

    public SensorData() {
    }

    public SensorData(Long busId, SensorType sensorType, Double value, LocalDateTime timestamp) {
        this.busId = busId;
        this.sensorType = sensorType;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAnomaly() {
        return anomaly;
    }

    public void setAnomaly(boolean anomaly) {
        this.anomaly = anomaly;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
