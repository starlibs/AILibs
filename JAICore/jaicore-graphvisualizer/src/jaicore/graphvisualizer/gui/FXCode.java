package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class FXCode {

    private TabPane tabPane;

    public void open(){
        //create BorderPane
        BorderPane pane = new BorderPane();


        //top
        ToolBar toolBar = new ToolBar();
        fillToolbar(toolBar.getItems());
        pane.setTop(toolBar);

        //center
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0,0.25);
        //left
        tabPane = new TabPane();

        splitPane.getItems().add(tabPane);

        pane.setCenter(splitPane);


        Scene scene = new Scene(pane, 800,300);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();


    }

    /**
     * Creates the controll-buttons and adds them to the given List
     * @param nodeList
     *      A list which shall contain the nodes of the buttons
     */
    private void fillToolbar(List<Node> nodeList){
        //playbutton
        Button playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("test");
            }
        });
        nodeList.add(playButton);

        //stepButton
        Button stepButton = new Button("Step");
        stepButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Step");
            }
        });
        nodeList.add(stepButton);

        //stopButton
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Stop");
            }
        });
        nodeList.add(stopButton);

        //BackButton
        Button backButton = new Button("Back");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("stop");
            }
        });
        nodeList.add(backButton);

        //loadButton
        Button loadButton = new Button("load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("load");
            }
        });
        nodeList.add(loadButton);

        //saveButton
        Button saveButton = new Button("save");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("save");
            }
        });
        nodeList.add(saveButton);


    }


    public TabPane getTabPane() {
        return tabPane;
    }


}
