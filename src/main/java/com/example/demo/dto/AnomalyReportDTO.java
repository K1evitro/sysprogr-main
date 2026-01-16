package com.example.demo.dto;

public class AnomalyReportDTO {

    private Long busId;
    private Long anomaliesCount;

    public AnomalyReportDTO(Long busId, Long anomaliesCount) {
        this.busId = busId;
        this.anomaliesCount = anomaliesCount;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public Long getAnomaliesCount() {
        return anomaliesCount;
    }

    public void setAnomaliesCount(Long anomaliesCount) {
        this.anomaliesCount = anomaliesCount;
    }
}
