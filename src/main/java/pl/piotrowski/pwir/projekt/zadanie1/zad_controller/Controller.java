package pl.piotrowski.pwir.projekt.zadanie1.zad_controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import pl.piotrowski.pwir.projekt.zadanie1.zad_model.Model;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;


public class Controller implements Observer {
    @FXML
    protected ListView<Long> numberList;
    @FXML
    protected Button startButton;
    @FXML
    protected Label resultLabel;
    private ObservableList<Long> observableList;
    private Model model;
    private ResultObserver resultObserver;


    public void start(ActionEvent actionEvent) {


        startButton.setDisable(true);
        //Platform.runLater(() -> {
        numberList.setItems(null);
        numberList.refresh();
        observableList.clear();
        resultLabel.setText("0");
        //});

        model.reset();
        model.start();
    }

    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            if (arg instanceof BigInteger) {
                observableList.add(((BigInteger) arg).longValue());
                numberList.setItems(observableList);
                numberList.refresh();
            }
        });
        if (model.isFinished()) {
            //Platform.runLater(() -> {
                startButton.setDisable(false);
            //});
        }


    }

    public class ResultObserver implements Observer {

        public void update(Observable o, Object arg) {
            if (arg instanceof String) {
                String text = (String) arg;
                Platform.runLater(() -> {
                    resultLabel.setText(text);
                });
            }
        }
    }


    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public ResultObserver getResultObserver() {
        return resultObserver;
    }

    public void initResultObserver() {
        this.resultObserver = new ResultObserver();
    }

    public ObservableList<Long> getObservableList() {
        return observableList;
    }

    public void initObservableList() {
        this.observableList = FXCollections.observableArrayList();
    }


}
