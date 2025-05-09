package main;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
}
