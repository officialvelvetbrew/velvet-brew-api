package com.cafe.velvetbrew.common.enums;

public enum DashboardPeriod {

    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year");

    private final String sqlUnit;

    DashboardPeriod(String sqlUnit) {
        this.sqlUnit = sqlUnit;
    }

    public String getSqlUnit() {
        return sqlUnit;
    }
}
