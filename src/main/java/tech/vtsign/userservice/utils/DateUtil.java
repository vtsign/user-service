package tech.vtsign.userservice.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public class DateUtil {
    public static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public static List<LocalDateTime> getDateBetween(String type) {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.atStartOfDay(),
                endDate = today.atTime(LocalTime.MAX);
        switch (type) {
            case "date":
                startDate = today.atStartOfDay();
                endDate = LocalTime.MAX.atDate(today);
                break;
            case "week":
                startDate = today.with(previousOrSame(MONDAY)).atStartOfDay();
                endDate = today.with(nextOrSame(SUNDAY)).atTime(LocalTime.MAX);
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
            case "year":
                startDate = today.withDayOfYear(1).atStartOfDay();
                endDate = today.withDayOfYear(today.lengthOfYear()).atTime(LocalTime.MAX);
                break;
        }
        return List.of(startDate, endDate);
    }
}
