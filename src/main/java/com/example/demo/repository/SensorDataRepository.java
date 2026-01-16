package com.example.demo.repository;

import com.example.demo.dto.AnomalyReportDTO;
import com.example.demo.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    
    List<SensorData> findByBusId(Long busId);

    @Query(value = "SELECT * FROM sensor_data WHERE bus_id = :busId ORDER BY timestamp DESC LIMIT 1", 
           nativeQuery = true)
    Optional<SensorData> findLatestByBusId(@Param("busId") Long busId);

    List<SensorData> findByAnomalyTrue();

    @Query("SELECT s FROM SensorData s WHERE s.timestamp >= :from AND s.timestamp <= :to ORDER BY s.timestamp DESC")
    List<SensorData> findBetweenTimestamps(
        @Param("from") LocalDateTime from, 
        @Param("to") LocalDateTime to
    );

    @Query("SELECT s FROM SensorData s WHERE s.bus.id = :busId AND s.timestamp >= :from AND s.timestamp <= :to ORDER BY s.timestamp DESC")
    List<SensorData> findByBusIdAndTimestampBetween(
        @Param("busId") Long busId, 
        @Param("from") LocalDateTime from, 
        @Param("to") LocalDateTime to
    );

    @Query("SELECT new com.example.demo.dto.AnomalyReportDTO(s.busId, COUNT(s)) " +
       "FROM SensorData s " +
       "WHERE s.anomaly = true AND s.timestamp BETWEEN :from AND :to " +
       "GROUP BY s.busId")
List<AnomalyReportDTO> getAnomalyReportByBus(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

}
