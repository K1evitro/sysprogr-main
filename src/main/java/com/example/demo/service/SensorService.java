package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.model.SensorData;
import com.example.demo.repository.SensorDataRepository;
import com.example.demo.Telegram.NotificationBot;

@Service
public class SensorService {

    private static final Logger log = LoggerFactory.getLogger(SensorService.class);

    private final SensorDataRepository sensorDataRepository;
    private final NotificationBot notificationBot;

    public SensorService(SensorDataRepository sensorDataRepository,
                         NotificationBot notificationBot) {
        this.sensorDataRepository = sensorDataRepository;
        this.notificationBot = notificationBot;
    }

    public List<SensorData> getAll() {
        return sensorDataRepository.findAll();
    }

    public SensorData getSensorData(Long id) {
        return sensorDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor data not found"));
    }

    public SensorData createSensorData(SensorData sensorData) {
        SensorData saved = sensorDataRepository.save(sensorData);

        if (saved.isAnomaly() && saved.getBus() != null) {
            String message = String.format(
                    "Аномалия датчика\n" +
                    "Автобус ID: %d\n" +
                    "Тип: %s\n" +
                    "Значение: %.2f\n" +
                    "Время: %s",
                    saved.getBus().getId(),
                    saved.getSensorType(),
                    saved.getValue(),
                    saved.getTimestamp()
            );
            log.info("Отправка уведомления в Telegram об аномалии: {}", message);
            notificationBot.sendNotification(message);
        } else {
            log.info("Создана запись датчика без аномалии или без автобуса, уведомление в Telegram не отправляется");
        }

        return saved;
    }

    public Optional<SensorData> getSensorDataById(Long id) {
        return sensorDataRepository.findById(id);
    }

    public SensorData updateSensorData(Long id, SensorData updatedSensorData) {
        return sensorDataRepository.findById(id)
                .map(sensorData -> {
                    sensorData.setBus(updatedSensorData.getBus());
                    sensorData.setSensorType(updatedSensorData.getSensorType());
                    sensorData.setValue(updatedSensorData.getValue());
                    sensorData.setTimestamp(updatedSensorData.getTimestamp());
                    sensorData.setAnomaly(updatedSensorData.isAnomaly());

                    SensorData saved = sensorDataRepository.save(sensorData);

                    if (saved.isAnomaly() && saved.getBus() != null) {
                        String message = String.format(
                                "Обновлена запись с аномалией\n" +
                                "Автобус ID: %d\n" +
                                "Тип: %s\n" +
                                "Значение: %.2f\n" +
                                "Время: %s",
                                saved.getBus().getId(),
                                saved.getSensorType(),
                                saved.getValue(),
                                saved.getTimestamp()
                        );
                        log.info("Отправка уведомления в Telegram об обновлённой аномалии: {}", message);
                        notificationBot.sendNotification(message);
                    } else {
                        log.info("Обновлена запись датчика без аномалии или без автобуса, уведомление в Telegram не отправляется");
                    }

                    return saved;
                })
                .orElse(null);
    }

    public boolean deleteSensorData(Long id) {
        if (sensorDataRepository.existsById(id)) {
            sensorDataRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<SensorData> getSensorDataByBusId(Long busId) {
        return sensorDataRepository.findByBusId(busId);
    }

    public void addFileToSensorData(Long sensorDataId, String filePath) {
        SensorData sensorData = sensorDataRepository.findById(sensorDataId)
                .orElseThrow(() -> new RuntimeException("Sensor data not found"));
        sensorData.setFilePath(filePath);
        sensorDataRepository.save(sensorData);
    }

    public Optional<SensorData> getLatestSensorDataByBusId(Long busId) {
        return sensorDataRepository.findLatestByBusId(busId);
    }

    public List<SensorData> getAllAnomalies() {
        return sensorDataRepository.findByAnomalyTrue();
    }

    public List<SensorData> getSensorHistoryByPeriod(LocalDateTime from, LocalDateTime to) {
        return sensorDataRepository.findBetweenTimestamps(from, to);
    }

    public List<SensorData> getSensorHistoryByBusAndPeriod(
            Long busId,
            LocalDateTime from,
            LocalDateTime to) {
        return sensorDataRepository.findByBusIdAndTimestampBetween(busId, from, to);
    }
}
