package main;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.paint.Color;

public class Event {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private Color color;
    
    // 新增連續事件欄位
    private boolean isContinuous;
    private LocalDate endDate;
    
    public Event(LocalDate date, LocalTime time, String description, Color color) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.color = color;
        this.isContinuous = false;
        this.endDate = date; // 預設結束日期與開始日期相同
    }
    
    // 新增支援日期區間的建構函式
    public Event(LocalDate startDate, LocalDate endDate, LocalTime time, String description, Color color) {
        this.date = startDate;
        this.endDate = endDate;
        this.time = time;
        this.description = description;
        this.color = color;
        this.isContinuous = !startDate.equals(endDate);
    }
    
    // Getters
    public LocalDate getDate() {
        return date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean isContinuous() {
        return isContinuous;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    /**
     * 檢查事件是否在指定日期
     * @param checkDate 要檢查的日期
     * @return 如果事件在指定日期上，則返回true
     */
    public boolean isOnDate(LocalDate checkDate) {
        if (isContinuous) {
            // 如果是連續事件，檢查日期是否在範圍內
            return !checkDate.isBefore(date) && !checkDate.isAfter(endDate);
        } else {
            // 如果是單一日期事件，直接比較日期
            return date.equals(checkDate);
        }
    }
    
    /**
     * 獲取事件持續天數
     * @return 事件持續的天數
     */
    public int getDurationDays() {
        if (!isContinuous) return 1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(date, endDate) + 1;
    }
    
    // Setters
    public void setDate(LocalDate date) {
        this.date = date;
        // 如果結束日期早於新的開始日期，重新設定結束日期
        if (this.endDate.isBefore(date)) {
            this.endDate = date;
        }
        // 重新評估是否為連續事件
        this.isContinuous = !this.date.equals(this.endDate);
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setEndDate(LocalDate endDate) {
        // 確保結束日期不早於開始日期
        if (endDate.isBefore(this.date)) {
            this.endDate = this.date;
        } else {
            this.endDate = endDate;
        }
        // 重新評估是否為連續事件
        this.isContinuous = !this.date.equals(this.endDate);
    }
    
    public void setContinuous(boolean isContinuous) {
        this.isContinuous = isContinuous;
        // 如果設置為非連續但結束日期與開始日期不同，重設結束日期
        if (!isContinuous && !this.date.equals(this.endDate)) {
            this.endDate = this.date;
        }
    }
    
    @Override
    public String toString() {
        return time.toString() + " - " + description;
    }
}