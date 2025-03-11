package com.lucas.callcenter_report.domain.util;

public class TimeUtils {
    public static String formatDuration(int totalSeconds) {
        if (totalSeconds < 0) return "00:00:00";
        
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}