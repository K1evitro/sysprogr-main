package com.example.demo.enums;

public enum AlertAction {
    OK("ok", "Нормально"),
    WARNING("warning", "Предупреждение"),
    ERROR("error", "Ошибка");

    private final String value;
    private final String displayName;

    AlertAction(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
