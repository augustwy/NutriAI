package com.nexon.nutriai.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtils {
    
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    
    /**
     * 将 LocalDateTime 转换为字符串
     * 
     * @param localDateTime 本地日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }
    
    /**
     * 将 LocalDateTime 转换为字符串（使用默认格式）
     * 
     * @param localDateTime 本地日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATE_TIME_FORMAT);
    }
    
    /**
     * 将字符串转换为 LocalDateTime
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseToLocalDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
    
    /**
     * 将字符串转换为 LocalDateTime（使用默认格式）
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        return parseToLocalDateTime(dateTimeStr, DEFAULT_DATE_TIME_FORMAT);
    }
    
    /**
     * 将 LocalDate 转换为字符串
     * 
     * @param localDate 本地日期
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate localDate, String pattern) {
        if (localDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(formatter);
    }
    
    /**
     * 将 LocalDate 转换为字符串（使用默认格式）
     * 
     * @param localDate 本地日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate localDate) {
        return format(localDate, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 将字符串转换为 LocalDate
     * 
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDate对象
     */
    public static LocalDate parseToLocalDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }
    
    /**
     * 将字符串转换为 LocalDate（使用默认格式）
     * 
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    public static LocalDate parseToLocalDate(String dateStr) {
        return parseToLocalDate(dateStr, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 将 LocalTime 转换为字符串
     * 
     * @param localTime 本地时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalTime localTime, String pattern) {
        if (localTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localTime.format(formatter);
    }
    
    /**
     * 将 LocalTime 转换为字符串（使用默认格式）
     * 
     * @param localTime 本地时间
     * @return 格式化后的字符串
     */
    public static String format(LocalTime localTime) {
        return format(localTime, DEFAULT_TIME_FORMAT);
    }
    
    /**
     * 将字符串转换为 LocalTime
     * 
     * @param timeStr 时间字符串
     * @param pattern 格式模式
     * @return LocalTime对象
     */
    public static LocalTime parseToLocalTime(String timeStr, String pattern) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(timeStr, formatter);
    }
    
    /**
     * 将字符串转换为 LocalTime（使用默认格式）
     * 
     * @param timeStr 时间字符串
     * @return LocalTime对象
     */
    public static LocalTime parseToLocalTime(String timeStr) {
        return parseToLocalTime(timeStr, DEFAULT_TIME_FORMAT);
    }
    
    /**
     * 将 Date 转换为 LocalDateTime
     * 
     * @param date Date对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * 将 LocalDateTime 转换为 Date
     * 
     * @param localDateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取当前时间戳（毫秒）
     * 
     * @return 当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前日期时间
     * 
     * @return 当前 LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * 获取当前日期
     * 
     * @return 当前 LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * 计算两个日期之间的天数差
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * 在指定日期上增加天数
     * 
     * @param date 原始日期
     * @param days 增加的天数
     * @return 新的日期
     */
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }
    
    /**
     * 在指定日期时间上增加小时数
     * 
     * @param dateTime 原始日期时间
     * @param hours 增加的小时数
     * @return 新的日期时间
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
}
