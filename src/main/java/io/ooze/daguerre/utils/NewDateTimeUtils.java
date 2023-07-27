package io.ooze.daguerre.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;

/**
 * datetime utils
 *
 * @date 2022/1/13 0013 13:49
 * @author zhangjw
 */
public final class NewDateTimeUtils {
	private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = TimeFormat.DATETIME_PATTERN_LINE.formatter;

	private NewDateTimeUtils() {

	}

	/**
	 * 字符串转带日期时间
	 *
	 */
	public static LocalDateTime parseTime(String timeStr) {
		return LocalDateTime.parse(timeStr, DEFAULT_DATETIME_FORMATTER);
	}

	/**
	 * 根据自定义字符串转带日期时间
	 *
	 */
	public static LocalDateTime parseTime(String timeStr, TimeFormat format) {
		return LocalDateTime.parse(timeStr, format.formatter);
	}

	/**
	 * 带日期时间转字符串
	 *
	 */
	public static String parseTime(LocalDateTime time) {
		return DEFAULT_DATETIME_FORMATTER.format(time);
	}

	/**
	 * 根据自定义带日期时间转字符串
	 *
	 */
	public static String parseTime(LocalDateTime time, TimeFormat format) {
		return format.formatter.format(time);
	}


	public static LocalDate parseDate(String timeStr) {
		return parseDate(timeStr, TimeFormat.DATE_PATTERN_LINE);
	}

	public static LocalDate parseDate(String timeStr, TimeFormat format) {
		return LocalDate.parse(timeStr, format.formatter);
	}

	public static String parseDate(LocalDate date) {
		return parseDate(date, TimeFormat.DATE_PATTERN_LINE);
	}

	public static String parseDate(LocalDate date, TimeFormat format) {
		return format.formatter.format(date);
	}

	/**
	 * 获取当前带日期时间字符串
	 *
	 */
	public static String getCurrentDateTimeStr() {
		return DEFAULT_DATETIME_FORMATTER.format(LocalDateTime.now());
	}

	/**
	 * 根据自定义获取当前时间字符串
	 *
	 */
	public static String getCurrentDateTimeStr(TimeFormat format) {
		return format.formatter.format(LocalDateTime.now());
	}
	
	
	
	/**
	 * 带日期时间转字符串
	 *
	 */
	public static String dateTime2Str(LocalDateTime time) {
		return parseTime(time);
	}
	
	public static String dateTime2Str(Date date) {
		return parseTime(date2LocalDateTime(date));
	}
	
	/**
	 * 日期转字符串
	 *
	 */
	public static String date2Str(LocalDateTime time) {
		return parseTime(time, TimeFormat.DATE_PATTERN_LINE);
	}
	
	public static String date2Str(Date date) {
		return parseTime(date2LocalDateTime(date), TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 字符串转带日期时间
	 *
	 */
	public static Date str2DateTime(String str) {
		return localDateTime2Date(parseTime(str));
	}
	
	public static LocalDateTime str2NewDateTime(String str) {
		return parseTime(str);
	}
	
	/**
	 * 根据字符串转日期
	 *
	 */
	public static Date str2Date(String str) {
		return localDate2Date(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}
	
	public static LocalDate str2NewDate(String str) {
		return parseDate(str, TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 带日期时间转字符串（yyyyMMddHHmmss）
	 *
	 */
	public static String dateTime2StrWithMilsecAllNone(LocalDateTime time) {
		return parseTime(time, TimeFormat.DATETIME_PATTERN_ALL_NODE);
	}

	/**
	 * 日期转字符串（yyyyMMddHHmmss）
	 *
	 */
	public static String dateTime2StrWithMilsecAllNone(Date date) {
		return parseTime(date2LocalDateTime(date), TimeFormat.DATETIME_PATTERN_ALL_NODE);
	}
	
	/**
	 * 日期转字符串（yyyyMMdd）
	 *
	 */
	public static String date2StrNone(LocalDateTime time) {
		return parseTime(time, TimeFormat.DATE_PATTERN_NONE);
	}
	
	/**
	 * 日期转字符串（yyyyMMdd）
	 *
	 */
	public static String date2StrNone(Date date) {
		return parseTime(date2LocalDateTime(date), TimeFormat.DATE_PATTERN_NONE);
	}
	
	/**
	 * 带日期时间转字符串（HHmmss）
	 *
	 */
	public static String time2StrNone(LocalDateTime time) {
		return parseTime(time, TimeFormat.TIME_PATTERN_NONE);
	}
	
	/**
	 * 时间转字符串（HHmmss）
	 *
	 */
	public static String time2StrNone(Date date) {
		return parseTime(date2LocalDateTime(date), TimeFormat.TIME_PATTERN_NONE);
	}
	
	/**
	 * 带日期时间转字符串（MM月dd日）
	 *
	 */
	public static String date2StrWithoutYearChinese(LocalDateTime time) {
		return parseTime(time, TimeFormat.DATE_PATTERN_WITHOUT_YEAR_CHINESE);
	}
	
	/**
	 * 日期转字符串（MM月dd日）
	 *
	 */
	public static String date2StrWithoutYearChinese(Date date) {
		return parseTime(date2LocalDateTime(date), TimeFormat.DATE_PATTERN_WITHOUT_YEAR_CHINESE);
	}
	
	/**
	 * CST字符串转字符串（yyyy-MM-dd）
	 *
	 */
	public static String cstStr2DateStr(String str) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
		return parseTime(date2LocalDateTime(sdf.parse(str)), TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 日期转自定义字符串
	 *
	 */
	public static String dateTime2StrCustomize(Date date, TimeFormat format) {
		return parseTime(date2LocalDateTime(date), format);
	}
	
	/**
	 * 自定义字符串转日期
	 *
	 */
	public static Date str2DateTimeCustomize(String str, TimeFormat format) {
		return localDateTime2Date(parseTime(str, format));
	}
	
	/**
	 * 获取当前日期
	 *
	 */
	public static Date getCurrentDate() {
		return localDateTime2Date(LocalDateTime.now());
	}
	
	/**
	 * 获取当前日期自定义字符串
	 *
	 */
	public static String getCurrentDateStr(TimeFormat format) {
		return parseTime(LocalDateTime.now(), format);
	}
	
	/**
	 * 获取当前时间毫秒数
	 *
	 */
	public static long getCurrentMillis() {
		return Instant.now().toEpochMilli();
	}

	public static long getCurrentSeconds() {
		return LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
	}

	public static long toSeconds(LocalDateTime time) {
		return time.toEpochSecond(ZoneOffset.of("+8"));
	}

	/**
	 * 毫秒数转字符串
	 *
	 */
	public static String millis2Str(long millis) {
		return parseTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
	}
	
	/**
	 * 毫秒数转自定义字符串
	 *
	 */
	public static String millis2Str(long millis, TimeFormat format) {
		return parseTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()), format);
	}
	
	
	
	
	/**
	 * 获取当前日期字符串（yyyy-MM-dd）
	 *
	 */
	public static String getCurrentDateStr() {
		return getCurrentDateStr(TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 获取昨天的日期
	 *
	 */
	public static Date getYesterday() {
		return localDateTime2Date(getNewYesterday());
	}
	
	/**
	 * 获取昨天带日期时间
	 *
	 */
	public static LocalDateTime getNewYesterday() {
		return minusDay(LocalDateTime.now(), 1);
	}
	
	/**
	 * 获取过去某天带日期时间
	 *
	 */
	public static LocalDateTime getNewPastOneDay(int day) {
		return minusDay(LocalDateTime.now(), day);
	}
	
	/**
	 * 获取过去某天日期
	 *
	 */
	public static Date getPastOneDay(int day) {
		return localDateTime2Date(getNewPastOneDay(day));
	}
	
	/**
	 * 获取当前星期的第一天日期字符串
	 *
	 */
	public static String getFirstDayStrOfCurrentWeek() {
		return parseTime(LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1),
				TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 获取当前星期的最后一天日期字符串
	 *
	 */
	public static String getLastDayStrOfCurrentWeek() {
		return parseTime(LocalDateTime.now().plusDays(7 - LocalDateTime.now().getDayOfWeek().getValue()),
				TimeFormat.DATE_PATTERN_LINE);
	}
	
	/**
	 * 获取当前星期的第一天日期字
	 *
	 */
	public static Date getFirstDayOfCurrentWeek() {
		return localDateTime2Date(LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1));
	}
	
	/**
	 * 获取当前星期的第一天带日期时间
	 *
	 */
	public static LocalDateTime getNewFirstDayOfCurrentWeek() {
		return LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1);
	}
	
	/**
	 * 获取当前星期的最后一天日期
	 *
	 */
	public static Date getLastDayOfCurrentWeek() {
		return localDateTime2Date(LocalDateTime.now().plusDays(7 - LocalDateTime.now().getDayOfWeek().getValue()));
	}
	
	/**
	 * 获取当前星期的最后一天带日期时间
	 *
	 */
	public static LocalDateTime getNewLastDayOfCurrentWeek() {
		return LocalDateTime.now().plusDays(7 - LocalDateTime.now().getDayOfWeek().getValue());
	}
	
	/**
	 * 获取某天的开始时间
	 *
	 */
	public static LocalDateTime getNewDayOfBeginning(String str) {
		return parseTime(str + " 00:00:00");
	}

	public static LocalDateTime getNewDayOfBeginning(LocalDate date) {
		return LocalDateTime.of(date, LocalTime.MIN);
	}

	public static LocalDateTime getNewDayOfBeginning() {
		return getNewDayOfBeginning(LocalDate.now());
	}
	
	/**
	 * 获取某天的开始时间
	 *
	 */
	public static Date getDayOfBeginning(String str) {
		return localDateTime2Date(parseTime(str + " 00:00:00"));
	}

	public static Date getDayOfBeginning(Date date) {
		return localDateTime2Date(LocalDateTime.of(date2LocalDate(date), LocalTime.MIN));
	}

	public static Date getDayOfBeginning() {
		return getDayOfBeginning(new Date());
	}
	
	/**
	 * 获取某天的结束时间
	 *
	 */
	public static LocalDateTime getNewDayOfEnd(String str) {
		return parseTime(str + " 23:59:59");
	}

	public static LocalDateTime getNewDayOfEnd(LocalDate date) {
		return LocalDateTime.of(date, LocalTime.MAX);
	}

	public static LocalDateTime getNewDayOfEnd() {
		return getNewDayOfEnd(LocalDate.now());
	}
	
	/**
	 * 获取某天的结束时间
	 *
	 */
	public static Date getDayOfEnd(String str) {
		return localDateTime2Date(parseTime(str + " 23:59:59"));
	}

	public static Date getDayOfEnd(Date date) {
		return localDateTime2Date(LocalDateTime.of(date2LocalDate(date), LocalTime.MAX));
	}

	public static Date getDayOfEnd() {
		return getDayOfEnd(new Date());
	}
	
	/**
	 * 某天加上几天后的时间
	 *
	 */
	public static LocalDateTime plusDay(Date date, int days) {
		return date2LocalDateTime(date).plusDays(days);
	}
	
	/**
	 * 某天加上几天后的时间
	 *
	 */
	public static LocalDateTime plusDay(LocalDateTime time, int days) {
		return time.plusDays(days);
	}

	public static LocalDate plusDay(LocalDate date, int days) {
		return date.plusDays(days);
	}
	
	/**
	 * 某天加上几月后的时间
	 *
	 */
	public static LocalDateTime plusMonth(Date date, int months) {
		return date2LocalDateTime(date).plusMonths(months);
	}
	
	/**
	 * 某天加上几月后的时间
	 *
	 */
	public static LocalDateTime plusMonth(LocalDateTime time, int months) {
		return time.plusMonths(months);
	}
	
	/**
	 * 某天加上几年后的时间
	 *
	 */
	public static LocalDateTime plusYear(Date date, int months) {
		return date2LocalDateTime(date).plusYears(months);
	}
	
	/**
	 * 某天加上几年后的时间
	 *
	 */
	public static LocalDateTime plusYear(LocalDateTime time, int months) {
		return time.plusYears(months);
	}
	
	/**
	 * 某天减去几天后的时间
	 *
	 */
	public static LocalDateTime minusDay(Date date, int days) {
		return date2LocalDateTime(date).minusDays(days);
	}
	
	/**
	 * 某天减去几天后的时间
	 *
	 */
	public static LocalDateTime minusDay(LocalDateTime time, int days) {
		return time.minusDays(days);
	}
	
	/**
	 * 某天减去几月后的时间
	 *
	 */
	public static LocalDateTime minusMonth(Date date, int days) {
		return date2LocalDateTime(date).minusMonths(days);
	}
	
	/**
	 * 某天减去几月后的时间
	 *
	 */
	public static LocalDateTime minusMonth(LocalDateTime time, int days) {
		return time.minusMonths(days);
	}
	
	/**
	 * 某天减去几年后的时间
	 *
	 */
	public static LocalDateTime minusYear(Date date, int days) {
		return date2LocalDateTime(date).minusYears(days);
	}
	
	/**
	 * 某天减去几年后的时间
	 *
	 */
	public static LocalDateTime minusYear(LocalDateTime time, int days) {
		return time.minusYears(days);
	}
	
	/**
	 * 计算两个时间毫秒数间隔的分钟数，
	 *
	 */
	public static long minutesBetween(Date begin, Date end) {
		return Math.floorDiv(end.getTime() - begin.getTime(), 60000L);
	}
	
	/**
	 * 计算某个时间毫秒间隔当前的分钟数
	 *
	 */
	public static long minutesAgo(Date date) {
		return minutesBetween(date, getCurrentDate());
	}
	
	/**
	 * 计算两个时间间隔的天数
	 *
	 */
	public static long daysBetween(LocalDateTime begin, LocalDateTime end) {
		return Duration.between(begin, end).toDays();
	}
	
	/**
	 * 计算两个时间间隔的天数
	 *
	 */
	public static long daysBetween(Instant begin, Instant end) {
		return Duration.between(begin, end).toDays();
	}
	
	/**
	 * 计算两个时间间隔的天数
	 *
	 */
	public static long daysBetween(Date begin, Date end) {
		return daysBetween(date2LocalDateTime(begin), date2LocalDateTime(end));
	}
	
	/**
	 * 计算两个时间间隔的天数
	 *
	 */
	public static long daysBetween(String begin, String end) {
		return daysBetween(parseTime(begin), parseTime(end));
	}
	
	/**
	 * 计算两个时间间隔的天数
	 *
	 */
	public static long daysBetween(String begin, String end, TimeFormat format) {
		return daysBetween(parseTime(begin, format), parseTime(end, format));
	}

	public static long monthsBetween(LocalDate begin, LocalDate end) {
		return begin.until(end, ChronoUnit.MONTHS);
	}

	public static long yearsBetween(LocalDate begin, LocalDate end) {
		return begin.until(end, ChronoUnit.YEARS);
	}


	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static String getFirstDayStrOfWeek(String str) {
		return parseDate(getNewFirstDayOfWeek(str));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static String getFirstDayStrOfWeek(Date date) {
		return parseDate(getNewFirstDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static String getFirstDayStrOfWeek(LocalDate date) {
		return parseDate(getNewFirstDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static Date getFirstDayOfWeek(String str) {
		return localDate2Date(getNewFirstDayOfWeek(str));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static Date getFirstDayOfWeek(Date date) {
		return localDate2Date(getNewFirstDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static Date getFirstDayOfWeek(LocalDate date) {
		return localDate2Date(getNewFirstDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static LocalDate getNewFirstDayOfWeek(String str) {
		return getNewFirstDayOfWeek(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static LocalDate getNewFirstDayOfWeek(Date date) {
		return getNewFirstDayOfWeek(date2LocalDate(date));
	}

	/**
	 * 获取某天所在周的周初
	 *
	 */
	public static LocalDate getNewFirstDayOfWeek(LocalDate date) {
		return date.minusDays(date.getDayOfWeek().getValue() - 1);
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static String getLastDayStrOfWeek(String str) {
		return parseDate(getNewLastDayOfWeek(str), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static String getLastDayStrOfWeek(Date date) {
		return parseDate(getNewLastDayOfWeek(date), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static String getLastDayStrOfWeek(LocalDate date) {
		return parseDate(getNewLastDayOfWeek(date), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static Date getLastDayOfWeek(String str) {
		return localDate2Date(getNewLastDayOfWeek(str));
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static Date getLastDayOfWeek(Date date) {
		return localDate2Date(getNewLastDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static Date getLastDayOfWeek(LocalDate date) {
		return localDate2Date(getNewLastDayOfWeek(date));
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static LocalDate getNewLastDayOfWeek(String str) {
		return getNewLastDayOfWeek(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static LocalDate getNewLastDayOfWeek(Date date) {
		return getNewLastDayOfWeek(date2LocalDate(date));
	}

	/**
	 * 获取某天所在周的周末
	 *
	 */
	public static LocalDate getNewLastDayOfWeek(LocalDate date) {
		return date.plusDays(DayOfWeek.SUNDAY.getValue() - date.getDayOfWeek().getValue());
	}


	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static String getFirstDayStrOfMonth(String str) {
		return parseDate(getNewFirstDayOfMonth(str));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static String getFirstDayStrOfMonth(Date date) {
		return parseDate(getNewFirstDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static String getFirstDayStrOfMonth(LocalDate date) {
		return parseDate(getNewFirstDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static Date getFirstDayOfMonth(String str) {
		return localDate2Date(getNewFirstDayOfMonth(str));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static Date getFirstDayOfMonth(Date date) {
		return localDate2Date(getNewFirstDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static Date getFirstDayOfMonth(LocalDate date) {
		return localDate2Date(getNewFirstDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static LocalDate getNewFirstDayOfMonth(String str) {
		return getNewFirstDayOfMonth(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static LocalDate getNewFirstDayOfMonth(Date date) {
		return getNewFirstDayOfMonth(date2LocalDate(date));
	}

	/**
	 * 获取某天所在月的月初
	 *
	 */
	public static LocalDate getNewFirstDayOfMonth(LocalDate date) {
		return date.with(TemporalAdjusters.firstDayOfMonth());
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static String getLastDayStrOfMonth(String str) {
		return parseDate(getNewLastDayOfMonth(str), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static String getLastDayStrOfMonth(Date date) {
		return parseDate(getNewLastDayOfMonth(date), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static String getLastDayStrOfMonth(LocalDate date) {
		return parseDate(getNewLastDayOfMonth(date), TimeFormat.DATE_PATTERN_LINE);
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static Date getLastDayOfMonth(String str) {
		return localDate2Date(getNewLastDayOfMonth(str));
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static Date getLastDayOfMonth(Date date) {
		return localDate2Date(getNewLastDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static Date getLastDayOfMonth(LocalDate date) {
		return localDate2Date(getNewLastDayOfMonth(date));
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static LocalDate getNewLastDayOfMonth(String str) {
		return getNewLastDayOfMonth(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static LocalDate getNewLastDayOfMonth(Date date) {
		return getNewLastDayOfMonth(date2LocalDate(date));
	}

	/**
	 * 获取某天所在月的月末
	 *
	 */
	public static LocalDate getNewLastDayOfMonth(LocalDate date) {
		return date.with(TemporalAdjusters.lastDayOfMonth());
	}

	public static String getFirstDayStrOfYear(String str) {
		return parseDate(getNewFirstDayOfYear(str));
	}

	public static String getFirstDayStrOfYear(Date date) {
		return parseDate(getNewFirstDayOfYear(date));
	}

	public static String getFirstDayStrOfYear(LocalDate date) {
		return parseDate(getNewFirstDayOfYear(date));
	}

	public static Date getFirstDayOfYear(String str) {
		return localDate2Date(getNewFirstDayOfYear(str));
	}

	public static Date getFirstDayOfYear(Date date) {
		return localDate2Date(getNewFirstDayOfYear(date));
	}

	public static Date getFirstDayOfYear(LocalDate date) {
		return localDate2Date(getNewFirstDayOfYear(date));
	}

	public static LocalDate getNewFirstDayOfYear(String str) {
		return getNewFirstDayOfYear(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	public static LocalDate getNewFirstDayOfYear(Date date) {
		return getNewFirstDayOfYear(date2LocalDate(date));
	}

	public static LocalDate getNewFirstDayOfYear(LocalDate date) {
		return date.with(TemporalAdjusters.firstDayOfYear());
	}

	public static String getLastDayStrOfYear(String str) {
		return parseDate(getNewFirstDayOfYear(str), TimeFormat.DATE_PATTERN_LINE);
	}

	public static String getLastDayStrOfYear(Date date) {
		return parseDate(getNewFirstDayOfYear(date), TimeFormat.DATE_PATTERN_LINE);
	}

	public static String getLastDayStrOfYear(LocalDate date) {
		return parseDate(getNewFirstDayOfYear(date), TimeFormat.DATE_PATTERN_LINE);
	}

	public static Date getLastDayOfYear(String str) {
		return localDate2Date(getNewFirstDayOfYear(str));
	}

	public static Date getLastDayOfYear(Date date) {
		return localDate2Date(getNewFirstDayOfYear(date));
	}

	public static Date getLastDayOfYear(LocalDate date) {
		return localDate2Date(getNewLastDayOfYear(date));
	}

	public static LocalDate getNewLastDayOfYear(String str) {
		return getNewLastDayOfYear(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	public static LocalDate getNewLastDayOfYear(Date date) {
		return getNewLastDayOfYear(date2LocalDate(date));
	}

	public static LocalDate getNewLastDayOfYear(LocalDate date) {
		return date.with(TemporalAdjusters.lastDayOfYear());
	}

	public static String getFirstDayStrOfQuarter(String str) {
		return parseDate(getNewFirstDayOfQuarter(str));
	}

	public static String getFirstDayStrOfQuarter(Date date) {
		return parseDate(getNewFirstDayOfQuarter(date));
	}

	public static String getFirstDayStrOfQuarter(LocalDate date) {
		return parseDate(getNewFirstDayOfYear(date));
	}

	public static Date getFirstDayOfQuarter(String str) {
		return localDate2Date(getNewFirstDayOfQuarter(str));
	}

	public static Date getFirstDayOfQuarter(Date date) {
		return localDate2Date(getNewFirstDayOfQuarter(date));
	}

	public static Date getFirstDayOfQuarter(LocalDate date) {
		return localDate2Date(getNewFirstDayOfQuarter(date));
	}

	public static LocalDate getNewFirstDayOfQuarter(String str) {
		return getNewFirstDayOfQuarter(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	public static LocalDate getNewFirstDayOfQuarter(Date date) {
		return getNewFirstDayOfQuarter(date2LocalDate(date));
	}

	public static LocalDate getNewFirstDayOfQuarter(LocalDate date) {
		if (date.getMonthValue() <= 3) {
			return LocalDate.of(date.getYear(), 1, 1);
		} else if (date.getMonthValue() <= 6) {
			return LocalDate.of(date.getYear(), 4, 1);
		} else if (date.getMonthValue() <= 9) {
			return LocalDate.of(date.getYear(), 7, 1);
		} else {
			return LocalDate.of(date.getYear(), 10, 1);
		}
	}

	public static String getLastDayStrOfQuarter(String str) {
		return parseDate(getNewFirstDayOfQuarter(str), TimeFormat.DATE_PATTERN_LINE);
	}

	public static String getLastDayStrOfQuarter(Date date) {
		return parseDate(getNewFirstDayOfQuarter(date), TimeFormat.DATE_PATTERN_LINE);
	}

	public static String getLastDayStrOfQuarter(LocalDate date) {
		return parseDate(getNewFirstDayOfQuarter(date), TimeFormat.DATE_PATTERN_LINE);
	}

	public static Date getLastDayOfQuarter(String str) {
		return localDate2Date(getNewFirstDayOfQuarter(str));
	}

	public static Date getLastDayOfQuarter(Date date) {
		return localDate2Date(getNewFirstDayOfQuarter(date));
	}

	public static Date getLastDayOfQuarter(LocalDate date) {
		return localDate2Date(getNewLastDayOfQuarter(date));
	}

	public static LocalDate getNewLastDayOfQuarter(String str) {
		return getNewLastDayOfQuarter(parseDate(str, TimeFormat.DATE_PATTERN_LINE));
	}

	public static LocalDate getNewLastDayOfQuarter(Date date) {
		return getNewLastDayOfQuarter(date2LocalDate(date));
	}

	public static LocalDate getNewLastDayOfQuarter(LocalDate date) {
		LocalDate lastDate = null;
		if (date.getMonthValue() <= 3) {
			lastDate = LocalDate.of(date.getYear(), 3, 1);
		} else if (date.getMonthValue() <= 6) {
			lastDate = LocalDate.of(date.getYear(), 6, 1);
		} else if (date.getMonthValue() <= 9) {
			lastDate = LocalDate.of(date.getYear(), 9, 1);
		} else {
			lastDate = LocalDate.of(date.getYear(), 12, 1);
		}
		return getNewLastDayOfMonth(lastDate);
	}







	
	public static int getYear() {
		return getYear(LocalDateTime.now());
	}
	
	public static int getYear(Date date) {
		return getYear(date2LocalDateTime(date));
	}

	public static int getYear(LocalDateTime time) {
		return time.getYear();
	}
	
	public static int getMonth() {
		return getMonth(LocalDateTime.now());
	}
	
	public static int getMonth(Date date) {
		return getMonth(date2LocalDateTime(date));
	}

	public static int getMonth(LocalDateTime time) {
		return time.getMonthValue();
	}
	
	public static int getDayOfYear() {
		return getDayOfYear(LocalDateTime.now());
	}
	
	public static int getDayOfYear(Date date) {
		return getDayOfYear(date2LocalDateTime(date));
	}

	public static int getDayOfYear(LocalDateTime time) {
		return time.getDayOfYear();
	}
	
	public static int getDayOfMonth() {
		return getDayOfMonth(LocalDateTime.now());
	}
	
	public static int getDayOfMonth(Date date) {
		return getDayOfMonth(date2LocalDateTime(date));
	}

	public static int getDayOfMonth(LocalDateTime time) {
		return time.getDayOfMonth();
	}
	
	public static int getDayOfWeek() {
		return getDayOfWeek(LocalDateTime.now());
	}
	
	public static int getDayOfWeek(Date date) {
		return getDayOfWeek(date2LocalDateTime(date));
	}

	public static int getDayOfWeek(LocalDateTime time) {
		return time.getDayOfWeek().getValue();
	}
	
	public static int getHour() {
		return getHour(LocalDateTime.now());
	}
	
	public static int getHour(Date date) {
		return getHour(date2LocalDateTime(date));
	}

	public static int getHour(LocalDateTime time) {
		return time.getHour();
	}
	
	public static int getMinute() {
		return getMinute(LocalDateTime.now());
	}
	
	public static int getMinute(Date date) {
		return getMinute(date2LocalDateTime(date));
	}

	public static int getMinute(LocalDateTime time) {
		return time.getMinute();
	}
	
	public static int getSecond() {
		return getSecond(LocalDateTime.now());
	}
	
	public static int getSecond(Date date) {
		return getSecond(date2LocalDateTime(date));
	}

	public static int getSecond(LocalDateTime time) {
		return time.getSecond();
	}
	
	
	
	public static boolean isWeekend(LocalDateTime time) {
		return time.getDayOfWeek().equals(DayOfWeek.SATURDAY) || time.getDayOfWeek().equals(DayOfWeek.SUNDAY);
	}
	
	public static boolean isWeekend(Date date) {
		return isWeekend(date2LocalDateTime(date));
	}
	
	public static boolean isWeekend(String str) {
		return isWeekend(parseTime(str));
	}
	
	/**
	 * LocalDateTime 转换成  Date
	 *
	 */
	public static Date localDateTime2Date(LocalDateTime time) {
		return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static Date localDate2Date(LocalDate date) {
		return Date.from(LocalDateTime.of(date, LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * Date 转换成 LocalDateTime
	 *
	 */
	public static LocalDateTime date2LocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public static LocalDate date2LocalDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}

	
	public enum TimeFormat {

		/**
		 * 短时间格式
		 */
		DATE_PATTERN_LINE("yyyy-MM-dd"),
		DATE_PATTERN_WITHOUT_DAY_LINE("yyyy-MM"),
		DATE_PATTERN_SLASH("yyyy/MM/dd"),
		DATE_PATTERN_DOUBLE_SLASH("yyyy\\MM\\dd"),
		DATE_PATTERN_CHINESE("yyyy年MM月dd日"),
		DATE_PATTERN_WITHOUT_DAY_CHINESE("yyyy年MM月"),
		DATE_PATTERN_WITHOUT_YEAR_CHINESE("MM月dd日"),
		DATE_PATTERN_NONE("yyyyMMdd"),
		DATE_PATTERN_SHORT("yyMMdd"),
		DATE_PATTERN_WITHOUT_DAY_NONE("yyyyMM"),

		TIME_PATTERN_NONE("HHmmss"),
		TIME_PATTERN("HH:mm:ss"),
		TIME_PATTERN_WITH_MILSEC_COLON("HH:mm:ss:SSS"),

		/**
		 * 长时间格式
		 */
		DATETIME_PATTERN_LINE("yyyy-MM-dd HH:mm:ss"),
		DATETIME_PATTERN_SLASH("yyyy/MM/dd HH:mm:ss"),
		DATETIME_PATTERN_DOUBLE_SLASH("yyyy\\MM\\dd HH:mm:ss"),
		DATETIME_PATTERN_NONE("yyyyMMdd HH:mm:ss"),
		DATETIME_PATTERN_ALL_NODE("yyyyMMddHHmmss"),
		DATETIME_PATTERN_CHINESE("yyyy年MM月dd日 HH时mm分ss秒"),
		DATETIME_PATTERN_SIMPLE("yyyyMMdd/HHmmss"),

		/**
		 * 长时间格式 带毫秒
		 */
		DATETIME_PATTERN_WITH_MILSEC_LINE("yyyy-MM-dd HH:mm:ss.SSS"),
		DATETIME_PATTERN_WITH_MILSEC_SLASH("yyyy/MM/dd HH:mm:ss.SSS"),
		DATETIME_PATTERN_WITH_MILSEC_DOUBLE_SLASH("yyyy\\MM\\dd HH:mm:ss.SSS"),
		DATETIME_PATTERN_WITH_MILSEC_NONE("yyyyMMdd HH:mm:ss.SSS"),
		DATETIME_PATTERN_WITH_MILSEC_ALL_NONE("yyyyMMddHHmmssSSS");

		private transient DateTimeFormatter formatter;

		TimeFormat(String pattern) {
			formatter = DateTimeFormatter.ofPattern(pattern);
		}
	}
}
