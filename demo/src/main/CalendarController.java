package main;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import main.services.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;



/**
 * 主要的日曆控制器類，負責協調其他組件
 */
public class CalendarController {

    // FXML注入的UI組件
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private TextField eventDateField;
    @FXML private TextField eventEndDateField;
    @FXML private CheckBox continuousEventCheckBox;
    @FXML private Label endDateLabel;
    @FXML private TextField eventNameField;
    @FXML private TextField eventTimeField;
    @FXML private ColorPicker eventColorPicker;
    @FXML private ListView<Event> eventListView;
    @FXML private Label eventListDateLabel;
    @FXML private Button collaborationToggleButton;
    
    // 輔助類實例
    private EventManager eventManager;
    private UIFactory uiFactory;
    private DateNavigator dateNavigator;
    
    // 當前選擇的事件
    private Event selectedEvent;
    // 日期格式轉換器
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 初始化方法，由FXML載入器自動調用
     */
    public void initialize() {
        // 創建輔助類實例

        if (!UserSession.getInstance().isLoggedIn()) {
            System.err.println("Warning: No user logged in");
            return;
        }
        
        // 從 UserSession 獲取用戶郵箱（這是唯一的重要修改）
        String userEmail = UserSession.getInstance().getCurrentUserEmail();
        
        // 創建輔助類實例（保持你原有的代碼）
        eventManager = new EventManager();
        
        // 設置當前用戶（修改這行）
        eventManager.setCurrentUser(userEmail);

        dateNavigator = new DateNavigator(eventManager);
        uiFactory = new UIFactory(eventManager, this);
        
        // 初始化編輯面板
        initializeEditPanel();
        
        // 設置月份切換按鈕
        setupNavigationButtons();
        
        // 初始顯示日曆
        updateCalendar();
        initializeCollaborationButton();
    }

    private void initializeCollaborationButton() {
        if (collaborationToggleButton != null) {
            updateCollaborationButtonText();
            collaborationToggleButton.setOnAction(e -> handleCollaborationToggle());
        }
    }
        @FXML
    private void handleCollaborationToggle() {
        // 防止快速多次點擊
        if (collaborationToggleButton.isDisabled()) {
            return;
        }
        
        collaborationToggleButton.setDisable(true);
        
        if (eventManager.isCollaborationMode()) {
            eventManager.enablePersonalMode();
            showModeChangeMessage("已切換到個人模式", false);
        } else {
            eventManager.enableCollaborationMode();
            showModeChangeMessage("已切換到協作模式 - 所有用戶的協作事件將會共享顯示", true);
        }
        
        updateCollaborationButtonText();
        updateCalendar(); // 確保更新日曆和事件列表
        
        System.out.println("Switched to: " + eventManager.getCurrentModeText());
        
        // 使用 Platform.runLater 確保 UI 更新完成後再啟用按鈕
        Platform.runLater(() -> {
            collaborationToggleButton.setDisable(false);
        });
    }
    
    private void showModeChangeMessage(String message, boolean isCollaboration) {
        // 使用 JavaFX Alert 顯示提示訊息
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("模式切換");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 設置不同的樣式，根據協作模式或個人模式
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        String style = isCollaboration 
            ? "-fx-background-color: #FFF3E0; -fx-border-color: #FF9800;" 
            : "-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50;";
        dialogPane.setStyle(style);
        
        alert.showAndWait();
    }

    private void updateCollaborationButtonText() {
        if (collaborationToggleButton != null) {
            if (eventManager.isCollaborationMode()) {
                collaborationToggleButton.setText("📝 個人模式");
                collaborationToggleButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            } else {
                collaborationToggleButton.setText("👥 協作模式");
                collaborationToggleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }
        }
    }
    /**
     * 初始化事件編輯面板
     */
    private void initializeEditPanel() {
        // 設置默認顏色
        eventColorPicker.setValue(Color.CORNFLOWERBLUE);
        
        // 設置默認日期（今天）
        LocalDate today = LocalDate.now();
        eventDateField.setText(today.format(dateFormatter));
        eventEndDateField.setText(today.format(dateFormatter));
        
        // 設置默認時間
        eventTimeField.setText("12:00");
        
        // 初始禁用結束日期欄位
        continuousEventCheckBox.setSelected(false);
        eventEndDateField.setDisable(true);
        endDateLabel.setDisable(true);
        
        // 更新事件列表日期標籤
        updateEventListDateLabel();
        
        // 設置ListView與事件列表綁定
        eventListView.setItems(eventManager.getCurrentDateEvents());
        
        // 設置ListView選擇事件監聽
        eventListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedEvent = newValue;
                    populateEventForm(newValue);
                }
            }
        );
    }
    
    /**
     * 設置月份導航按鈕
     */
    private void setupNavigationButtons() {
        if (previousButton != null) {
            previousButton.setOnAction(e -> {
                dateNavigator.goToPreviousMonth();
                updateCalendar();
            });
            
            // 添加按鈕效果
            addButtonEffects(previousButton);
        }
        
        if (nextButton != null) {
            nextButton.setOnAction(e -> {
                dateNavigator.goToNextMonth();
                updateCalendar();
            });
            
            // 添加按鈕效果
            addButtonEffects(nextButton);
        }
    }
    
    /**
     * 為按鈕添加視覺效果
     */
    private void addButtonEffects(Button button) {
        String baseStyle = button.getStyle();
        String hoverStyle = baseStyle + "-fx-background-color: #4a7da8;";
        String pressedStyle = baseStyle + "-fx-background-color: #3a6d98; -fx-translate-y: 1px;";
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnMousePressed(e -> button.setStyle(pressedStyle));
        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                button.setStyle(hoverStyle);
            } else {
                button.setStyle(baseStyle);
            }
        });
    }
    
    /**
     * 更新日曆顯示
     */
    private void updateCalendar() {
        // 更新月年標籤
        if (monthYearLabel != null) {
            dateNavigator.updateMonthYearLabel(monthYearLabel);
        }
        
        // 重新繪製日曆網格
        if (calendarGrid != null) {
            calendarGrid.getChildren().clear();
            
            // 添加星期標題
            String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (int i = 0; i < dayHeaders.length; i++) {
                Label dayLabel = new Label(dayHeaders[i]);
                dayLabel.getStyleClass().add("day-header");
                dayLabel.setMaxWidth(Double.MAX_VALUE);
                dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
                calendarGrid.add(dayLabel, i, 0);
            }
            
            // 獲取當月信息
            LocalDate currentDate = dateNavigator.getCurrentDate();
            int firstDayOfWeek = dateNavigator.getFirstDayOfMonthWeekday();
            int daysInMonth = dateNavigator.getDaysInMonth();
            
            // 添加日期格子
            int currentDay = 1;
            for (int week = 1; week <= 6 && currentDay <= daysInMonth; week++) {
                for (int dayOfWeek = 0; dayOfWeek < 7 && currentDay <= daysInMonth; dayOfWeek++) {
                    if (week == 1 && dayOfWeek < firstDayOfWeek) {
                        // 空格子（月初前的空白）
                        VBox emptyCell = uiFactory.createCell("");
                        calendarGrid.add(emptyCell, dayOfWeek, week);
                    } else {
                        // 日期格子
                        VBox dateCell = uiFactory.createDateCell(currentDay, currentDate);
                        calendarGrid.add(dateCell, dayOfWeek, week);
                        currentDay++;
                    }
                }
            }
        }
        
        // 確保事件列表也更新
        updateEventList();
    }
    
    /**
     * 處理日期點擊事件 - 只負責跳轉到日記頁面，不再設置事件編輯的日期
     */
    public void handleDateClick(LocalDate date) {
        // 跳轉到日記頁面
        dateNavigator.navigateToDiary(date);
    }
    
    /**
     * 解析日期字段的文本
     */
    private LocalDate parseEventDate() {
        try {
            return LocalDate.parse(eventDateField.getText(), dateFormatter);
        } catch (DateTimeParseException e) {
            // 如果解析失敗，返回當前日期
            eventDateField.setText(LocalDate.now().format(dateFormatter));
            return LocalDate.now();
        }
    }
    
    /**
     * 更新事件列表日期標籤
     */
    private void updateEventListDateLabel() {
        LocalDate date = parseEventDate();
        if (eventListDateLabel != null) {
            eventListDateLabel.setText(date.format(dateFormatter));
        }
    }
    
    /**
     * 清除事件表單
     */
    private void clearEventForm() {
        selectedEvent = null;
        eventNameField.clear();
        eventTimeField.setText("12:00");
        eventColorPicker.setValue(Color.CORNFLOWERBLUE);
        
        // 重置連續事件相關欄位
        continuousEventCheckBox.setSelected(false);
        eventEndDateField.setDisable(true);
        endDateLabel.setDisable(true);
        eventEndDateField.setText(eventDateField.getText()); // 設置為與開始日期相同
    }
    
    /**
     * 用事件數據填充表單
     */
    private void populateEventForm(Event event) {
        eventDateField.setText(event.getDate().format(dateFormatter));
        eventEndDateField.setText(event.getEndDate().format(dateFormatter));
        continuousEventCheckBox.setSelected(event.isContinuous());
        eventEndDateField.setDisable(!event.isContinuous());
        endDateLabel.setDisable(!event.isContinuous());
        
        eventNameField.setText(event.getDescription());
        eventTimeField.setText(String.format("%02d:%02d", 
                            event.getTime().getHour(), 
                            event.getTime().getMinute()));
        eventColorPicker.setValue(event.getColor());
    }
    
    /**
     * 選擇事件進行編輯
     */
    public void selectEventForEdit(Event event) {
        selectedEvent = event;
        populateEventForm(event);
        eventListView.getSelectionModel().select(event);
    }
    
    /**
     * 處理添加/更新事件按鈕
     */
    @FXML
    private void handleAddUpdateEvent() {
        String eventName = eventNameField.getText().trim();
        String timeString = eventTimeField.getText().trim();
        Color selectedColor = eventColorPicker.getValue();
        LocalDate eventDate = parseEventDate();
        
        if (eventName.isEmpty()) {
            return;
        }
        
        LocalTime time = parseTimeString(timeString);
        
        // 處理連續事件邏輯
        boolean isContinuous = continuousEventCheckBox.isSelected();
        LocalDate endDate = eventDate;
        if (isContinuous) {
            try {
                endDate = LocalDate.parse(eventEndDateField.getText(), dateFormatter);
                // 確保結束日期不早於開始日期
                if (endDate.isBefore(eventDate)) {
                    endDate = eventDate;
                    eventEndDateField.setText(eventDate.format(dateFormatter));
                }
            } catch (DateTimeParseException e) {
                endDate = eventDate;
                eventEndDateField.setText(eventDate.format(dateFormatter));
            }
        }
        
        if (selectedEvent == null) {            // 創建新事件
            Event newEvent;
            if (isContinuous) {
                newEvent = new Event(eventDate, endDate, time, eventName, selectedColor);
            } else {
                newEvent = new Event(eventDate, time, eventName, selectedColor);
            }            
            eventManager.addEvent(newEvent);
            
            // 顯示添加成功訊息，根據當前模式顯示不同提示
            if (eventManager.isCollaborationMode()) {
                // 使用 JavaFX Alert 顯示協作事件添加成功的提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("協作事件");
                alert.setHeaderText(null);
                alert.setContentText("協作事件已添加！\n所有用戶都能看到此事件。");
                
                // 設置樣式
                javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800;");
                
                alert.showAndWait();
            }
        } else {
            // 更新現有事件
            selectedEvent.setDate(eventDate);
            selectedEvent.setDescription(eventName);
            selectedEvent.setTime(time);
            selectedEvent.setColor(selectedColor);
            
            if (isContinuous) {
                selectedEvent.setEndDate(endDate);
                selectedEvent.setContinuous(true);
            } else {
                selectedEvent.setEndDate(eventDate);
                selectedEvent.setContinuous(false);
            }
            
            eventManager.updateEvent(selectedEvent);
        }
        
        // 清除表單
        clearEventForm();
        
        // 更新顯示
        updateEventList();
        updateCalendar();
    }
    
    /**
     * 更新事件列表
     */
    private void updateEventList() {
        LocalDate date = parseEventDate();
        eventManager.setSelectedDate(date);
    }
    
    /**
     * 處理"顯示"按鈕點擊事件
     */
    @FXML
    private void handleShowEvents() {
        updateEventList();
        updateEventListDateLabel();
    }
    
    /**
     * 處理"今天"按鈕點擊事件
     */
    @FXML
    private void handleSetTodayDate() {
        eventDateField.setText(LocalDate.now().format(dateFormatter));
    }
    
    /**
     * 解析時間字符串為LocalTime對象
     */
    private LocalTime parseTimeString(String timeString) {
        LocalTime time = LocalTime.of(12, 0); // 預設中午12點
        
        try {
            // 嘗試解析時間
            String[] parts = timeString.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) {
                    time = LocalTime.of(hour, minute);
                }
            }
        } catch (Exception e) {
            // 使用預設時間
        }
        
        return time;
    }
    
    /**
     * 處理刪除事件按鈕
     */
    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent != null) {
            eventManager.removeEvent(selectedEvent);
            clearEventForm();
            updateCalendar();
        }
    }
    
    /**
     * 處理Project按鈕點擊
     */
     @FXML
    private void handleProjectButton(ActionEvent event) throws IOException {
        // 取得事件來源節點以獲取當前場景
        javafx.scene.Node sourceNode = (javafx.scene.Node) event.getSource();
        // 將節點傳給 navigateToProject 方法
        dateNavigator.navigateToProject(sourceNode);
    }
    
    /**
     * 處理連續事件複選框的切換
     */
    @FXML
    private void handleContinuousEventToggle() {
        boolean isContinuous = continuousEventCheckBox.isSelected();
        eventEndDateField.setDisable(!isContinuous);
        endDateLabel.setDisable(!isContinuous);
        
        // 如果取消選中，設置結束日期與開始日期相同
        if (!isContinuous) {
            eventEndDateField.setText(eventDateField.getText());
        }
    }

    /**
     * 將結束日期設置為與開始日期相同
     */
    @FXML
    private void handleSetSameDate() {
        eventEndDateField.setText(eventDateField.getText());
    }

    /**
     * 解析結束日期字段的文本
     */
    private LocalDate parseEventEndDate() {
        try {
            return LocalDate.parse(eventEndDateField.getText(), dateFormatter);
        } catch (DateTimeParseException e) {
            // 如果解析失敗，設定為開始日期
            LocalDate startDate = parseEventDate();
            eventEndDateField.setText(startDate.format(dateFormatter));
            return startDate;
        }
    }

    /**
     * 設置日曆顯示的月份和年份
     * 這個方法供其他控制器（如 Controller）調用，當從 project 頁面回到日曆頁面時使用
     */
    public void setMonthAndYear(int month, int year) {
        // 將 DateNavigator 的當前日期設置為指定的年月
        dateNavigator.setCurrentDate(LocalDate.of(year, month, 1));
        // 更新日曆顯示
        updateCalendar();
    }

    /**
     * 顯示事件操作訊息
     */
    private void showEventActionMessage(String message, boolean isCollaboration) {
        // 使用 JavaFX Alert 顯示提示訊息
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("事件操作");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 設置不同的樣式，根據協作模式或個人模式
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        String style = isCollaboration 
            ? "-fx-background-color: #FFF3E0; -fx-border-color: #FF9800;" 
            : "-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50;";
        dialogPane.setStyle(style);
        
        alert.showAndWait();
    }

}