package main;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class CalendarController {

    @FXML
    private GridPane calendarGrid;
    
    @FXML
    private Label monthYearLabel;
    
    @FXML
    private Button previousButton;
    
    @FXML
    private Button nextButton;
    
    private LocalDate currentDate;
    
    public void initialize() {
        // 初始化為當前日期
        currentDate = LocalDate.now();
        
        // 設置按鈕動態效果
        setupButtonEffects();
        
        // 設置月份切換按鈕
        if (previousButton != null) {
            previousButton.setOnAction(e -> {
                currentDate = currentDate.minusMonths(1);
                updateCalendar();
            });
        }
        
        if (nextButton != null) {
            nextButton.setOnAction(e -> {
                currentDate = currentDate.plusMonths(1);
                updateCalendar();
            });
        }
        
        // 初始顯示日曆
        updateCalendar();
    }
    
    private void setupButtonEffects() {
        // 添加滑鼠懸停和點擊效果
        if (previousButton != null) {
            String baseStyle = previousButton.getStyle();
            String hoverStyle = baseStyle + "-fx-background-color: #4a7da8;";
            String pressedStyle = baseStyle + "-fx-background-color: #3a6d98; -fx-translate-y: 1px;";
            
            previousButton.setOnMouseEntered(e -> previousButton.setStyle(hoverStyle));
            previousButton.setOnMouseExited(e -> previousButton.setStyle(baseStyle));
            previousButton.setOnMousePressed(e -> previousButton.setStyle(pressedStyle));
            previousButton.setOnMouseReleased(e -> {
                if (previousButton.isHover()) {
                    previousButton.setStyle(hoverStyle);
                } else {
                    previousButton.setStyle(baseStyle);
                }
            });
        }
        
        if (nextButton != null) {
            String baseStyle = nextButton.getStyle();
            String hoverStyle = baseStyle + "-fx-background-color: #4a7da8;";
            String pressedStyle = baseStyle + "-fx-background-color: #3a6d98; -fx-translate-y: 1px;";
            
            nextButton.setOnMouseEntered(e -> nextButton.setStyle(hoverStyle));
            nextButton.setOnMouseExited(e -> nextButton.setStyle(baseStyle));
            nextButton.setOnMousePressed(e -> nextButton.setStyle(pressedStyle));
            nextButton.setOnMouseReleased(e -> {
                if (nextButton.isHover()) {
                    nextButton.setStyle(hoverStyle);
                } else {
                    nextButton.setStyle(baseStyle);
                }
            });
        }
    }
    
    private void updateCalendar() {
        // 清除先前的日曆內容
        calendarGrid.getChildren().clear();
        
        // 設置月份年份標題 - 使用英文格式
        if (monthYearLabel != null) {
            String month = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
            int year = currentDate.getYear();
            monthYearLabel.setText(month + " " + year);
        }
        
        // 添加星期標題 (0行) - 使用英文格式
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of((i + 1) % 7 + 1); // 從星期日開始
            Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, Locale.US));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.getStyleClass().add("day-header");
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // 獲取當月的信息
        YearMonth yearMonth = YearMonth.from(currentDate);
        int daysInMonth = yearMonth.lengthOfMonth();
        
        // 獲取當月第一天是星期幾 (0=星期日, 1=星期一, ..., 6=星期六)
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 調整為星期日為0
        
        // 填充日曆
        int day = 1;
        int row = 1; // 從第1行開始（第0行是星期標題）
        
        // 填充第一週前的空白
        for (int col = 0; col < dayOfWeekValue; col++) {
            VBox cell = createCell("");
            calendarGrid.add(cell, col, row);
        }
        
        // 填充日期
        for (int col = dayOfWeekValue; col < 7; col++) {
            if (day <= daysInMonth) {
                VBox cell = createDateCell(day, currentDate);
                calendarGrid.add(cell, col, row);
                day++;
            }
        }
        
        // 填充剩下的週
        while (day <= daysInMonth) {
            row++;
            for (int col = 0; col < 7 && day <= daysInMonth; col++) {
                VBox cell = createDateCell(day, currentDate);
                calendarGrid.add(cell, col, row);
                day++;
            }
        }
    }
    
    private VBox createCell(String text) {
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
    
    private VBox createDateCell(int day, LocalDate baseDate) {
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
        
        // 高亮顯示當天
        LocalDate cellDate = baseDate.withDayOfMonth(day);
        LocalDate today = LocalDate.now();
        
        if (cellDate.equals(today)) {
            cell.getStyleClass().add("today-cell");
            dateLabel.getStyleClass().add("today-label");
        }
        
        // 添加滑鼠懸停效果
        String baseStyle = cell.getStyle();
        String hoverStyle = baseStyle.isEmpty() ? 
            "-fx-border-color: lightgray; -fx-background-color: rgba(200, 220, 240, 0.3);" : 
            baseStyle + "-fx-background-color: rgba(200, 220, 240, 0.5);";
        
        cell.setOnMouseEntered(e -> cell.setStyle(hoverStyle));
        cell.setOnMouseExited(e -> cell.setStyle(baseStyle));
        
        // 添加點擊事件（可擴展為添加事件等功能）
        cell.setOnMouseClicked(e -> handleDateClick(cellDate));
        
        return cell;
    }
    
    private void handleDateClick(LocalDate date) {
        // 處理日期點擊事件，可以在這裡添加彈出窗口來新增/編輯事件
        System.out.println("Clicked date: " + date);
    }

    @FXML
    private void handleProjectButton(ActionEvent event) throws IOException {
        // 如果 center 部分已經被替換過，則不再加載
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/project.fxml"));
        Parent projectRoot = loader.load();

        // 獲取 ProjectController 並傳遞當前月份和年份
        Controller projectController = loader.getController();
        if (projectController != null) {
            int month = currentDate.getMonthValue();
            int year = currentDate.getYear();
            projectController.setMonth(month, year);  // 傳遞月份和年份
        }

        // 獲取當前場景，並將 Project 頁面設置為 BorderPane 的 center
        // Scene scene = ((Node) event.getSource()).getScene();
        // BorderPane root = (BorderPane) scene.getRoot();
        // root.setCenter(projectRoot);  // 只替換 center 部分
        Scene scene = calendarGrid.getScene();
        scene.setRoot(projectRoot);
    }

    @FXML
    private void handleIndexButton(ActionEvent event) throws IOException {
        // 加載 Calendar 頁面
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/calendar.fxml"));
        Parent calendarRoot = loader.load();

        // 獲取當前場景，並將整個場景替換為 Calendar 頁面
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(calendarRoot);  // 替換整個場景的根節點
    }

    public void setMonthAndYear(int month, int year) {
        this.currentDate = LocalDate.of(year, month, 1); // 確保 currentDate 被正確設置為完整的 LocalDate
        updateCalendar();
    }


}