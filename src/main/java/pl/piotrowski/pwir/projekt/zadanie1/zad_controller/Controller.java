package pl.piotrowski.pwir.projekt.zadanie1.zad_controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import pl.piotrowski.pwir.projekt.zadanie1.zad_model.Model;

import java.math.BigInteger;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class Controller implements Observer, Initializable {
    @FXML
    public Slider delaySpinner;
    @FXML
    public RadioButton longRadio;
    @FXML
    public ToggleGroup NumberOptions;
    @FXML
    public RadioButton integerRadio;
    @FXML
    public Button stopButton;
    @FXML
    protected ListView<Long> numberList;
    @FXML
    protected Button startButton;
    @FXML
    protected Label resultLabel;
    private ObservableList<Long> observableList;
    private Model model;
    private ResultObserver resultObserver;


    public void start() {
        startButton.setDisable(true);
        model.reset();
        Platform.runLater(() -> {
            numberList.setItems(null);
            numberList.refresh();
            observableList.clear();
            resultLabel.setText("0");
        });
        model.start();
    }

    public void stop() {
        model.stop();
        Platform.runLater(() -> {
            numberList.setItems(null);
            numberList.refresh();
            observableList.clear();
            resultLabel.setText("0");
        });
    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof BigInteger) {
            Platform.runLater(() -> {
                observableList.add(0, ((BigInteger) arg).longValue());
                numberList.setItems(observableList);
                numberList.refresh();
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        delaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> model.setDelay(newValue.longValue()));
    }

    public void numberTypeChangeAction() {
        if (longRadio.isSelected()) {
            model.setGeneratingLongs(true);
        } else {
            model.setGeneratingLongs(false);
        }
    }


    public class ResultObserver implements Observer {

        public void update(Observable o, Object arg) {
            if (arg instanceof String) {
                String text = (String) arg;
                Platform.runLater(() -> resultLabel.setText(text));
            }

            if (model.isFinished()) {
                Platform.runLater(() -> startButton.setDisable(false));
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

    public void initObservableList() {
        this.observableList = FXCollections.observableArrayList();
    }


}
