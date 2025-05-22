package main;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.Node; // 導入 Node 類
import javafx.scene.Scene; // 確保導入 Scene 類
import javafx.scene.control.TextArea;


import main.database.DiaryDatabase;
import main.models.ProjectModel;



public class Controller {

    @FXML
    private VBox taskContainer1;
    @FXML
    private VBox taskContainer2;
    @FXML
    private VBox taskContainer3;
    @FXML
    private VBox taskContainer4;

    @FXML
    private ProgressBar progressBar1;
    @FXML
    private ProgressBar progressBar2;
    @FXML
    private ProgressBar progressBar3;
    @FXML
    private ProgressBar progressBar4;

    @FXML
    private Label monthYearLabel;  // 用來顯示月份的 Label

    private int currentMonth;
    private int currentYear;
    private DiaryDatabase database = DiaryDatabase.getInstance();

    @FXML
    private TextField project1;
    @FXML
    private TextField project2;
    @FXML
    private TextField project3;
    @FXML
    private TextField project4;

    @FXML
    private TextArea about1;

    @FXML
    private TextArea about2;

    @FXML
    private TextArea about3;

    @FXML
    private TextArea about4;

    @FXML
    private TextField habit1;

    @FXML
    private TextField habit2;

    @FXML
    private TextField habit3;

    @FXML
    private TextField habit4;




    @FXML
    public void initialize() {
        setupProgressBar(taskContainer1, progressBar1);
        setupProgressBar(taskContainer2, progressBar2);
        setupProgressBar(taskContainer3, progressBar3);
        setupProgressBar(taskContainer4, progressBar4);

        VBox[] taskContainers = {taskContainer1, taskContainer2, taskContainer3, taskContainer4};

        for (VBox taskContainer : taskContainers) {
            for (var node : taskContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    hbox.getChildren().forEach(child -> {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            textField.setOnAction(event -> moveToNextTextField(taskContainer, textField));
                        }
                    });
                }
            }
        }
        //loadProjectContent(currentYear, currentMonth);
        
        // 設定所有輸入欄位的失焦事件處理
        setupBlurEventHandlers();

    }

    private void setupProgressBar(VBox taskContainer, ProgressBar progressBar) {
        for (var node : taskContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                hbox.getChildren().forEach(child -> {
                    if (child instanceof CheckBox) {
                        ((CheckBox) child).setOnAction(event -> updateProgress(taskContainer, progressBar));
                    } else if (child instanceof TextField) {
                        ((TextField) child).textProperty().addListener((observable, oldValue, newValue) -> updateProgress(taskContainer, progressBar));
                    }
                });
            }
        }
        updateProgress(taskContainer, progressBar);
    }

    private void updateProgress(VBox taskContainer, ProgressBar progressBar) {
        long totalTasks = taskContainer.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .filter(hbox -> hbox.getChildren().stream()
                        .filter(child -> child instanceof TextField)
                        .map(child -> (TextField) child)
                        .anyMatch(textField -> !textField.getText().trim().isEmpty()))
                .count();

        if (totalTasks == 0) {
            progressBar.setProgress(0);
            return;
        }

        long completedTasks = taskContainer.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .filter(hbox -> {
                    boolean isCheckBoxSelected = hbox.getChildren().stream()
                            .filter(child -> child instanceof CheckBox)
                            .map(child -> (CheckBox) child)
                            .anyMatch(CheckBox::isSelected);

                    boolean isTextFieldFilled = hbox.getChildren().stream()
                            .filter(child -> child instanceof TextField)
                            .map(child -> (TextField) child)
                            .anyMatch(textField -> !textField.getText().trim().isEmpty());

                    return isCheckBoxSelected && isTextFieldFilled;
                })
                .count();

        double progress = (double) completedTasks / totalTasks;
        progressBar.setProgress(progress);
    }

    private void moveToNextTextField(VBox taskContainer, TextField currentTextField) {
        boolean focusNext = false;
        for (var node : taskContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (var child : hbox.getChildren()) {
                    if (child instanceof TextField) {
                        TextField textField = (TextField) child;
                        if (focusNext) {
                            textField.requestFocus();
                            return;
                        }
                        if (textField == currentTextField) {
                            focusNext = true;
                        }
                    }
                }
            }
        }
    }


    //新加的

    public void setMonth(int month, int year) {
        this.currentMonth = month;
        this.currentYear = year;
        // 使用月份和年份設置標籤的文本
        String monthName = LocalDate.of(year, month, 1).getMonth().getDisplayName(TextStyle.FULL, Locale.US);
        monthYearLabel.setText(year +" " + monthName + " " + "project");
        loadProjectContent(year, month);

    }

    @FXML
    private void handleBackToCalendar(ActionEvent event) throws IOException {
        // 加載 Calendar 頁面
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/calendar.fxml"));
        Parent calendarRoot = loader.load();

        // 獲取當前場景，並將 Calendar 頁面設置為 BorderPane 的 center
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(calendarRoot);
    }

    @FXML
    private void handleMonthButton(ActionEvent event) throws IOException {
        // 創建 FXMLLoader 並設置共享的 CalendarController 實例
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/calendar.fxml"));
        CalendarController sharedCalendarController = new CalendarController();
        loader.setController(sharedCalendarController);

        // 加載 Calendar 頁面
        Parent monthRoot = loader.load();

        // 傳遞當前月份和年份
        sharedCalendarController.setMonthAndYear(currentMonth, currentYear);

        // 切換場景
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(monthRoot);
    }

    public void loadProjectContent(int year, int month) {
        // 從數據庫中加載選定日期的日記內容
        ProjectModel entry = database.getProjectEntry(year,month);
        
        if (entry != null) {
            // 填充UI元素
                project1.setText(entry.getProject1());
                System.out.println("get = "+ entry.getProject2());
                project2.setText(entry.getProject2());
                project3.setText(entry.getProject3());
                project4.setText(entry.getProject4());
                about1.setText(entry.getAbout1());
                about2.setText(entry.getAbout2());
                about3.setText(entry.getAbout3());
                about4.setText(entry.getAbout4());
                habit1.setText(entry.getHabit1());
                habit2.setText(entry.getHabit2());
                habit3.setText(entry.getHabit3());
                habit4.setText(entry.getHabit4());
    
            // } else {
            //     System.out.println("該日期的項目1為空");
            //     project1.setText("");
            // }
        } else {
            // 如果沒有找到該日期的條目，清空所有欄位
            project1.clear();
            System.out.println("沒");
        }
    }

    private void setupBlurEventHandlers() {
        // 為每個輸入欄位添加失焦事件處理器
        project1.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        project2.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        project3.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        project4.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        about1.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        about2.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        about3.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        about4.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        habit1.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        habit2.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        habit3.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });
        habit4.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveProjectEntry();
            }
        });

          
    }

    private void saveProjectEntry() {
        ProjectModel entry = new ProjectModel(currentYear, currentMonth);
        System.out.println(currentYear + " " + currentMonth);
        entry.setProject1(project1.getText());
        entry.setProject2(project2.getText());
        System.out.println("string = "+ entry.getProject2());
        entry.setProject3(project3.getText());
        entry.setProject4(project4.getText());
        entry.setAbout1(about1.getText());
        entry.setAbout2(about2.getText());
        entry.setAbout3(about3.getText());
        entry.setAbout4(about4.getText());
        entry.setHabit1(habit1.getText());
        entry.setHabit2(habit2.getText());
        entry.setHabit3(habit3.getText());
        entry.setHabit4(habit4.getText());

        
        
        // 保存到數據庫
        System.out.println(entry.projectName1);
        database.saveProject(entry);
    }

    
    
}
