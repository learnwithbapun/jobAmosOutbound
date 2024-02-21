package com.gbt.cdms.amos.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.scheduling.support.CronSequenceGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class AppUtils {

    private AppUtils() {
    }

    public static String toJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static LocalTime convertCronToScheduleTime(String expression) {
        log.info("Converting cronExpression: {}", expression);
        CronSequenceGenerator generator = new CronSequenceGenerator(expression);
        // Subtract one second from the current date
        Date now = new Date();
        Date slightlyEarlier = new Date(now.getTime() - 1000);
        Date date = generator.next(slightlyEarlier);
        Timestamp timestamp = new Timestamp(date.getTime());
        LocalTime earlier_run = AppUtils.formattedTime(timestamp);
        LocalTime scheduled_run = earlier_run.minusMinutes(10);
        log.info("Converted cronExpression: {} and got output as: {}", expression, scheduled_run);
        return scheduled_run;
    }
    
    public static LocalTime convertCronToTime(String expression) {
        log.info("Converting cronExpression: {}", expression);
        CronSequenceGenerator generator = new CronSequenceGenerator(expression);
        Date date = generator.next(new Date());
        Timestamp timestamp = new Timestamp(date.getTime());
        LocalTime scheduled_run = formattedTime(timestamp);
        log.info("Converted cronExpression: {} and got output as: {}", expression, scheduled_run);
        return scheduled_run;
    }

    public static LocalTime formattedTime(Date time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return LocalTime.parse(dateFormat.format(calendar.getTime()));
    }
}