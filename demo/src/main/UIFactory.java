package main;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * 用於創建UI組件的工廠类
 */
public class UIFactory {
    private EventManager eventManager;
    private CalendarController controller;
    
    public UIFactory(EventManager eventManager, CalendarController controller) {
        this.eventManager = eventManager;
        this.controller = controller;
    }
    
    /**
     * 創建一個空的格子
     */
    public VBox createCell(String text) {
        VBox cell = new VBox();
        cell.setPrefSize(100, 80);
        cell.getStyleClass().add("calendar-cell");
        cell.setAlignment(Pos.TOP_LEFT);
        
        if (!text.isEmpty()) {
            Label label = new Label(text);
            cell.getChildren().add(label);
        }
        
        return cell;
    }
    
    /**
     * 創建一個日期格子
     */
    public VBox createDateCell(int day, LocalDate baseDate) {
        String text = String.valueOf(day);
        VBox cell = createCell(text);
        
        // 調整日期位置
        cell.getChildren().clear(); // 清除預設標籤
        
        Label dateLabel = new Label(text);
        dateLabel.getStyleClass().add("date-label");
        
        VBox dateContainer = new VBox(dateLabel);
        dateContainer.setAlignment(Pos.TOP_RIGHT);
        dateContainer.setPrefWidth(Double.MAX_VALUE);
        
        cell.getChildren().add(dateContainer);
        
        // 計算此格子的日期
        LocalDate cellDate = baseDate.withDayOfMonth(day);
        LocalDate today = LocalDate.now();
        
        // 高亮顯示當天
        if (cellDate.equals(today)) {
            cell.getStyleClass().add("today-cell");
            dateLabel.getStyleClass().add("today-label");
        }
        
        // 不再根據eventManager的日期來高亮顯示，避免與事件編輯面板耦合
        // 如果需要反映當前選中的導航日期，可以增加新的方法
        
        // 添加滑鼠懸停效果
        String baseStyle = cell.getStyle();
        String hoverStyle = baseStyle.isEmpty() ? 
            "-fx-border-color: lightgray; -fx-background-color: rgba(200, 220, 240, 0.3);" : 
            baseStyle + "-fx-background-color: rgba(200, 220, 240, 0.5);";
        
        cell.setOnMouseEntered(e -> cell.setStyle(hoverStyle));
        cell.setOnMouseExited(e -> cell.setStyle(baseStyle));
        
        // 添加點擊事件（現在只跳轉至日記頁面，不再設置事件編輯面板的日期）
        cell.setOnMouseClicked(e -> controller.handleDateClick(cellDate));
        
        // 顯示該日期的事件
        displayEventsForDate(cell, cellDate);
        
        return cell;
    }
    
    /**
     * 顯示指定日期的所有事件
     */
    private void displayEventsForDate(VBox cell, LocalDate cellDate) {
        List<Event> events = eventManager.getEventsForDate(cellDate);
        for (Event event : events) {
            HBox eventLabel = createEventLabel(event);
            cell.getChildren().add(eventLabel);
        }
    }
    
    /**
     * 創建事件標籤
     */
    public HBox createEventLabel(Event event) {
        HBox eventBox = new HBox(5);
        eventBox.setMaxWidth(Double.MAX_VALUE);
        
        Label timeLabel = new Label(event.getTime().toString().substring(0, 5));
        timeLabel.setStyle("-fx-font-size: 8pt;");
        
        String eventText = event.getDescription();
        // 為連續事件添加標記
        if (event.isContinuous()) {
            int duration = event.getDurationDays();
            eventText += " (" + duration + "天)";
        }
        
        Label descLabel = new Label(eventText);
        descLabel.setStyle("-fx-font-size: 8pt;");
        
        // 設置背景顏色
        Color eventColor = event.getColor();
        String colorStyle = String.format(
            "-fx-background-color: rgba(%d, %d, %d, 0.7);",
            (int)(eventColor.getRed() * 255),
            (int)(eventColor.getGreen() * 255),
            (int)(eventColor.getBlue() * 255)
        );
        
        // 為連續事件添加特殊樣式
        if (event.isContinuous()) {
            colorStyle += "-fx-border-width: 1px; -fx-border-color: rgba(0, 0, 0, 0.3);";
            colorStyle += "-fx-border-radius: 3px; -fx-background-radius: 3px;";
        }
        
        eventBox.setStyle(colorStyle);
        eventBox.getChildren().addAll(timeLabel, descLabel);
        
        // 點擊事件標籤時選擇該事件進行編輯
        eventBox.setOnMouseClicked(e -> {
            e.consume(); // 防止觸發單元格的點擊事件
            controller.selectEventForEdit(event);
        });
        
        return eventBox;
    }
    
    /**
     * 創建星期標題標籤
     */
    public Label createDayHeaderLabel(DayOfWeek day) {
        Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, Locale.US));
        dayLabel.setAlignment(Pos.CENTER);
        dayLabel.setMaxWidth(Double.MAX_VALUE);
        dayLabel.getStyleClass().add("day-header");
        return dayLabel;
    }
}