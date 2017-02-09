package pl.piotrowski.pwir.projekt.zadanie1.zad_view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import pl.piotrowski.pwir.projekt.zadanie1.zad_controller.Controller;
import pl.piotrowski.pwir.projekt.zadanie1.zad_model.Model;

public class View extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
        GridPane gridPane = loader.load();


        Controller controller = loader.getController();
        controller.setModel(new Model());
        controller.initObservableList();
        controller.initResultObserver();
        controller.getModel().addObserver(controller);
        controller.getModel().addObserver(controller.getResultObserver());


        Scene scene = new Scene(gridPane, 600, 600);
        primaryStage.setTitle("Zadanie 1 PWiR");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
