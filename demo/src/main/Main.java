package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public String weather;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/Login.fxml"));
        loader.setController(null); // 加這行！
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("測試DayPlanView");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}