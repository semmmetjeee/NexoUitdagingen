package me.semmmetje.nexouitdagingen.quest;

import org.bukkit.configuration.file.FileConfiguration;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class PeriodService {
    private final FileConfiguration config;
    private final ZoneId zone;

    public PeriodService(FileConfiguration config) {
        this.config = config;
        ZoneId parsed;
        try { parsed = ZoneId.of(config.getString("settings.timezone", "Europe/Amsterdam")); }
        catch (Exception ex) { parsed = ZoneId.systemDefault(); }
        this.zone = parsed;
    }

    public String dailyKey() {
        ZonedDateTime now = ZonedDateTime.now(zone);
        int hour = Math.max(0, Math.min(23, config.getInt("settings.daily-reset-hour", 0)));
        if (now.getHour() < hour) now = now.minusDays(1);
        return now.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }

    public String weeklyKey() {
        ZonedDateTime now = ZonedDateTime.now(zone);
        DayOfWeek resetDay;
        try { resetDay = DayOfWeek.valueOf(config.getString("settings.weekly-reset-day", "MONDAY").toUpperCase()); }
        catch (Exception ex) { resetDay = DayOfWeek.MONDAY; }
        int hour = Math.max(0, Math.min(23, config.getInt("settings.weekly-reset-hour", 0)));
        LocalDate date = now.toLocalDate();
        while (date.getDayOfWeek() != resetDay) date = date.minusDays(1);
        ZonedDateTime reset = date.atTime(hour, 0).atZone(zone);
        if (now.isBefore(reset)) reset = reset.minusWeeks(1);
        return reset.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }

    public String globalKey() { return weeklyKey(); }
}
