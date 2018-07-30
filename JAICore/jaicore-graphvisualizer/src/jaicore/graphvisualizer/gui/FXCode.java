package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXCode extends Application{

    Slider timeline;
    TabPane tabPane;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        open();


    }

    public void open(){
        //FX-elements
        BorderPane root = new BorderPane();
        BorderPane top = new BorderPane();
        //Top
        ToolBar toolbar = new ToolBar();
        Slider speedSlider = new Slider();
        speedSlider.setMin(0);
        speedSlider.setMax(200);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setMajorTickUnit(10);
        speedSlider.setValue(150);

        Button playButton = new Button("play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                play();
            }
        });
        Button stepButton = new Button("step");
        Button backButton = new Button("back");
        Button resetButton = new Button("reset");
        Button stopButton = new Button("stop");
        Button saveButton = new Button("save");
        Button loadButton = new Button("load");

//      setting the top elements
        root.setTop(top);
        top.setTop(toolbar);
        toolbar.getItems().add(playButton);
        toolbar.getItems().add(stepButton);
        toolbar.getItems().add(backButton);
        toolbar.getItems().add(resetButton);
        toolbar.getItems().add(stopButton);
        toolbar.getItems().add(saveButton);
        toolbar.getItems().add(loadButton);
        top.setBottom(speedSlider);

//        Center
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.25);
        tabPane = new TabPane();
        BorderPane centerBorder = new BorderPane();
        SwingNode visuPanel = new SwingNode();

        splitPane.getItems().add(tabPane);
        splitPane.getItems().add(centerBorder);
        centerBorder.setCenter(visuPanel);
        root.setCenter(splitPane);

//        bottom
        timeline = new Slider();
        timeline.setShowTickLabels(true);
        timeline.setShowTickMarks(true);
        root.setBottom(timeline);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 800,600));
        stage.show();

    }

    private void play(){
        System.out.println("Test");
    }
}
