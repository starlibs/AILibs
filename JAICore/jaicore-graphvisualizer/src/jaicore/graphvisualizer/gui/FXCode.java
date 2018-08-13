package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FXCode {
    
//    Tabpane for additional tabs
    private TabPane tabPane;
    
//    timeline
    private Slider timeline;

//    EventBus
    private EventBus eventBus;

    private Thread playThread;

    private int index;
    private int maxIndex;

    private long sleepTime;

    //Visualization window
    private GraphVisualization visualization;

    public FXCode(Recorder rec){
        this.index = 0;
        this.maxIndex = 0;
        this.sleepTime = 50;

        this.eventBus = new EventBus();
        this.eventBus.register(rec);
        rec.registerInfoListener(this);

        //create BorderPane
        BorderPane root = new BorderPane();


//        top
        ToolBar toolBar = new ToolBar();
        fillToolbar(toolBar.getItems());
        root.setTop(toolBar);

//        center
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0,0.25);
//        left
        tabPane = new TabPane();

        splitPane.getItems().add(tabPane);
        visualization = new GraphVisualization();
        rec.registerReplayListener(visualization);
        splitPane.getItems().add(visualization.getViewPanel());

        root.setCenter(splitPane);


//        Bottom
        this.timeline = new Slider();
        this.timeline.setShowTickLabels(true);
        this.timeline.setShowTickMarks(true);
        this.timeline.setOnMouseReleased((MouseEvent event)->{
            System.out.println(timeline.getValue());
        });
        root.setBottom(this.timeline);
        


        Scene scene = new Scene(root, 800,300);
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
                //play runs in an own thread to make it stoppable
                Runnable run = ()->{
                    try{
                        while(index >= 0){
                            eventBus.post(new StepEvent(true, 1));
                            TimeUnit.MILLISECONDS.sleep(sleepTime);
                        }
                    }
                    catch(InterruptedException e){
//                e.printStackTrace();
                    }
                };

                playThread = new Thread(run);
                playThread.start();
            }
        });
        nodeList.add(playButton);
        //stepButton
        Button stepButton = new Button("Step");
        stepButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
               System.out.println("Step");
               eventBus.post(new StepEvent(true, 1));
               if(index != maxIndex)
                   updateIndex(1, false);
            }
        });
        nodeList.add(stepButton);

        //stopButton
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Stop");
                if(playThread!= null)
                    playThread.interrupt();
            }
        });
        nodeList.add(stopButton);

        //BackButton
        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("back");
                if(index == 0)
                    return;
                if(index == 1) {
                    reset();
                    return;
                }
                eventBus.post(new StepEvent(false, 1));
                updateIndex(-1, false);
            }
        });
        nodeList.add(backButton);

//        resetButton
        Button resetButton = new Button("reset");
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                reset();
            }
        });

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

    @Subscribe
    public void receiveInfoEvent(InfoEvent event){
        this.maxIndex = event.getMaxIndex();
        this.timeline.setMax(this.maxIndex);
        if(event.updateIndex())
            this.updateIndex(maxIndex, true);

    }

    public void updateIndex(int newIndex, boolean isRealIndex){
        if(! isRealIndex)
            newIndex += this.index;


        if (newIndex > this.maxIndex || newIndex < 0)
            return;

        this.index = newIndex;
        this.timeline.setValue(this.index);


    }

    public void reset(){
        this.updateIndex(0, true);
        this.visualization.reset();
        eventBus.post(new ResetEvent());
    }

    public TabPane getTabPane() {
        return tabPane;
    }


}
