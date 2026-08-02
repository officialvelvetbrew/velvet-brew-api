package com.cafe.velvetbrew.utils;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderNumberGenerator {

    private OrderNumberGenerator() {
    }

    public static String generate() {

        String date = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String random = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        return "VB-" + date + "-" + random;
    }
}