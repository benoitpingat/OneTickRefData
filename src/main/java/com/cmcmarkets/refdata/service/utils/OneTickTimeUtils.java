package com.cmcmarkets.refdata.service.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OneTickTimeUtils {

    static DateTimeFormatter yyyyMMddHHmmssDateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final long OPEN_DATE = 99999999000000L;

    static public long getNowYYYYMMDDhhmmss(){
        return Long.parseLong(LocalDateTime.now().format(yyyyMMddHHmmssDateFormatter));
    }

    static public String getRefUUID(String symbology, String symbol) {
        return symbology+symbol;
    }

}
